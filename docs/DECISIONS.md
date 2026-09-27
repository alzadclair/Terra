# TerraForge RPG — Architecture & Design Decisions Log

Este documento registra todas as decisões técnicas fundamentais, justificativas de arquitetura e padrões adotados no desenvolvimento do projeto.

---

## ADR-001: Plataforma e Toolchain
- **Decisão:** Minecraft 1.21.1 sobre **NeoForge 21.1.250+**, compilado com **Java 21 LTS** utilizando o plugin `net.neoforged.moddev:2.0.147`.
- **Justificativa:** Estabilidade, compatibilidade avançada com o ecossistema moderno de mods e suporte integral às novas APIs de Data Attachments, Data Components e Payload Networking introduzidas no ciclo 1.20.5+/1.21+.

## ADR-002: Persistência de Dados do Jogador (NeoForge Data Attachments)
- **Decisão:** Utilizar a API oficial de **AttachmentType** do NeoForge para anexar o objeto `PlayerRPGData` aos jogadores (`Player`).
- **Justificativa:** Substitui as antigas capabilities do Forge 1.20-. Oferece persistência nativa entre dimensões e mortes (`copyOnDeath`), serialização via `INBTSerializable` moderno e sincronização seletiva sem dependência de NBT manual vulnerável a erros.

## ADR-003: Persistência de Progressão de Mundo (SavedData)
- **Decisão:** Utilizar `SavedData` anexado ao `ServerLevel` sobre a dimensão primária/overworld para rastrear `WorldProgressionData` (Hardmode ativado, bosses derrotados, eventos concluídos, tipo de world evil).
- **Justificativa:** Mudanças de mundo afetam todos os jogadores e devem persistir no nível do servidor, imunes ao logout de jogadores individuais.

## ADR-004: Modelo de Rede Server-Authoritative
- **Decisão:** Todas as ações do jogador (gastar pontos de atributo, equipar artefatos especiais, ativar habilidades raciais) transitam via `CustomPacketPayload` validadas estritamente no servidor (`ServerPayloadHandler`).
- **Justificativa:** O cliente nunca dita valores finais ("me dê 500 de dano" ou "tenho 1000 pontos"). O cliente apenas envia pedidos de intenção com argumentos sanitizados.

## ADR-005: Terraria Realm — Dimensão em Camadas Contínuas
- **Decisão:** A dimensão `terraforge_rpg:terraria_realm` será gerada como uma estrutura vertical contínua, reproduzindo as camadas oficiais de Terraria:
  - Camada Superior: **Space** ($Y > 200$)
  - Camada de Superfície: **Surface** ($Y = 64$ a $200$)
  - Camada Subterrânea: **Underground** ($Y = 0$ a $63$)
  - Camada de Cavernas: **Cavern** ($Y = -54$ a $-1$)
  - Camada Profunda: **Underworld** ($Y \le -55$, com leito de lava e cinzas)
- **Acesso:** Portal craftável pós-early-game, permitindo transição fluida sem quebrar o Overworld vanilla.

## ADR-006: Separação sourceStats vs runtimeStats
- **Decisão:** Itens, armas e monstros mantêm seus valores originais de Terraria 1.4.5.8 em estruturas imutáveis (`sourceStats`). Um adaptador centralizado (`TerrariaStatConverter`) converte esses números para a escala de gameplay tridimensional do Minecraft (`runtimeStats`).
- **Justificativa:** Previne desvios arbitrários e garante que a fidelidade à versão canônica possa ser ajustada globalmente sem necessidade de reescrever milhares de arquivos de itens.

## ADR-007: Prevenção de Overflow e Potencial Infinito de Evolução
- **Decisão:** Todos os acumuladores de pontos de status e atributos para a raça Humana pós-nível 1000 utilizarão inteiros de 64 bits (`long`) ou números de ponto flutuante de dupla precisão (`double`) com clamping técnico de segurança na conversão para o motor de física do Minecraft.
- **Justificativa:** O atributo "Velocidade" não pode exceder limites que causem dessincronização de chunks ou quebra do motor de colisão, mesmo que o rank matemático do jogador seja ilimitado.

## ADR-008: CustomPacketPayload Networking
- **Decisão:** Comunicação cliente-servidor através da nova infraestrutura de `CustomPacketPayload` com `PayloadRegistrar` e execução prioritária na thread principal (`HandlerThread.MAIN`).
- **Justificativa:** Elimina vulnerabilidades de concorrência com o mundo do Minecraft e assegura que pacotes desconhecidos ou malformados sejam descartados com segurança.

## ADR-009: Validação e Alocação de Atributos via StatService
- **Decisão:** Alocações de pontos passam estritamente por `StatService.spendPoints`, verificando pontos disponíveis, teto racial e regras de Evolução antes de qualquer mutação.
- **Justificativa:** Impede exploits de alocação de pontos negativos, gastos acima do permitido ou ultrapassagem de caps não autorizadas.

## ADR-010: CPU Weighted Linear Blend Skinning (LBS) para Chefes e Criaturas Articuladas
- **Decisão:** A deformação e animação de malhas com múltiplos pesos de ossos por vértice (como o Eye of Cthulhu Fase 1 e Fase 2) é realizada via **CPU Linear Blend Skinning (LBS)** com separação estrita entre geometria imutável (`TerraSkinnedMeshData`) e buffers de trabalho mutáveis por entidade/modelo (`TerraSkinnedMeshInstance`), em vez de shaders de GPU customizados.
- **Justificativa e Detalhes de Implementação:**
  1. **Compatibilidade Ampla de Shaders e Drivers:** Compute shaders ou shaders customizados de skinning em GPU entram em conflito com Iris, Sodium, OptiFine e drivers integrados (Intel/AMD/Apple). O streaming via `VertexConsumer` do Minecraft é universalmente compatível.
  2. **Isolamento de Estado Multi-Entidade:** A geometria e matrizes de bind originais residem em `TerraSkinnedMeshData` (somente-leitura, compartilhado). Cada entidade ou instância de modelo (`EyeOfCthulhuModel`) instancia seus próprios buffers (`TerraSkinnedMeshInstance`). Múltiplos Eyes of Cthulhu coexistindo no mesmo chunk nunca compartilham nem corrompem os buffers de deformação uns dos outros.
  3. **Pré-Carregamento em Recarregamento de Recursos:** O carregamento e parsing de JSON não ocorre durante o primeiro frame de spawn de um boss. `TerraSkinnedMeshLoader.preload(...)` é invocado durante o evento `RegisterClientReloadListenersEvent`, pré-carregando `eye_of_cthulhu_p1.skin.json` e `eye_of_cthulhu_p2.skin.json` e medindo o tempo de parse (~5-15 ms em carregamento, 0 ms em runtime).
  4. **Matrizes de Bind Autênticas e Invariância de Rest Pose:** As matrizes de bind inverso originais do GLTF (acessores 40 e 32) são transformadas para o espaço de coordenadas do Minecraft via $M_{mc} = T \cdot M_{gltf} \cdot T^{-1}$ (onde $T$ mapeia $x \to x, y \to -z, z \to y$). Na pose de repouso, o erro de identidade $\max |S_{skin} - I| < 5 \times 10^{-5}$ (precisão limite de float de 32 bits para coordenadas de pivô $\approx 250$), garantindo zero distorção visual.
  5. **Skinning de Normais com Escala Não Uniforme:** A matriz normal de cada osso é computada a cada frame via $N_{skin} = (M_{skin}^{3\times 3})^{-T}$ utilizando `Matrix4f.normal(Matrix3f)` da JOML com zero alocações na heap. Isso assegura ortogonalidade perfeita de iluminação mesmo sob escalas agressivas (como $(0.88, 0.88, 1.35)$ em animações de charge).
  6. **Zero Alocação Heap por Frame:** `TerraSkinnedMeshInstance` pré-aloca arrays primitivos (`float[] skinnedPositions`, `float[] skinnedNormals`, `Matrix4f[] bonePalette`, `Matrix3f[] normalPalette`), eliminando qualquer criação de objetos no ciclo de renderização.

---

### ADR 7: Unified Skeletal Bind Pose Source of Truth
* **Data:** 2026-09-26
* **Status:** Aprovado e Implementado
* **Contexto:** Anteriormente, o rig do Eye of Cthulhu em runtime usava pivôs e matrizes locais aproximadas hardcoded, enquanto os testes aplicavam matrizes reais via patch de teste `applyBindMatrices(meshData)`. Isso criava divergência entre os ambientes de teste e runtime e deformava os vértices durante animações de mandíbula.
* **Decisão:**
  1. A fonte de verdade única para hierarquia de ossos, matrizes `bindLocal`, `bindWorld` e `inverseBind` é o arquivo `.skin.json` exportado do GLTF canônico.
  2. `EyeSkeletonFactory.create(meshData)` foi introduzido como o criador unificado do esqueleto para runtime e testes, eliminando o patch `applyBindMatrices`.
  3. A transformação local de cada osso separa a pose de bind dos deltas de animação:
     $$\mathbf{M}_{local} = \mathbf{M}_{bindLocal} \times \mathbf{M}_{animLocal}$$
     $$\mathbf{M}_{world} = \mathbf{M}_{parentWorld} \times \mathbf{M}_{local}$$
     $$\mathbf{M}_{skin} = \mathbf{M}_{world} \times \mathbf{M}_{invBindWorld}$$
  4. Quando os deltas são neutros ($\mathbf{M}_{animLocal} = \mathbf{I}$), $\mathbf{M}_{skin} = \mathbf{I}$ com erro absoluto $< 10^{-12}$.
  5. As rotações de mandíbula (superior e inferior) atuam estritamente como deltas de rotação em torno do pivô local $[0.018, 12.302, 0.144]$ e $[0.018, 10.149, -4.324]$, mantendo drift de pivô $< 10^{-5}$ em qualquer ângulo.

---

### ADR 8: Per-Entity Render State Isolation (`EyeRenderState`)
* **Data:** 2026-09-26
* **Status:** Aprovado e Implementado
* **Contexto:** `EyeOfCthulhuModel` mantinha campos mutáveis (`skeleton`, `animController`, `lastAgeInTicks`, `instanceP1`, `instanceP2`), causando contaminação cruzada quando múltiplos Eyes of Cthulhu existiam no mundo. O cálculo de deltaTime e interpolações de um boss corrompia a animação dos demais.
* **Decisão:**
  1. `EyeOfCthulhuModel` tornou-se completamente stateless. O modelo não mantém instâncias de mesh, controladores de animação ou tempo.
  2. Todo o estado mutável por entidade reside em `EyeRenderState`, gerenciado exclusivamente no client por `EyeRenderStateManager` mapeado pelo `UUID` da entidade.
  3. Ciclos de vida e prevenção de vazamento de memória:
     - `EntityLeaveLevelEvent`: remove o estado do cache quando o boss morre ou é descarregado.
     - `LevelEvent.Unload`: limpa todos os estados ao descarregar a dimensão/mundo.
     - `ClientPlayerNetworkEvent.LoggingOut`: limpa o cache ao sair de servidores.
     - `RegisterClientReloadListenersEvent` (F3+T): limpa instâncias e recarrega meshes.
  4. Comando de depuração para desenvolvedores `/terraforge debug eye state <state>` adicionado para validação em runtime de qualquer fase ou animação.

