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
