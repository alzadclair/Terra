# TerraForge RPG — Master Specification

**Mod ID:** `terraforge_rpg`  
**Package Base:** `com.terraforge.rpg`  
**Minecraft Version:** `1.21.1`  
**Mod Loader:** `NeoForge 21.1.250+`  
**Java Runtime:** `Java 21 LTS`  
**Canonical Reference:** `Terraria PC 1.4.5.8` (`docs/TERRARIA_REFERENCE_VERSION.md`)  

---

## 1. Visão Geral e Filosofia

O **TerraForge RPG** recria a experiência integral de Terraria dentro do ambiente tridimensional do Minecraft Java Edition, operando sob uma arquitetura limpa, data-driven, multiplayer-safe e estritamente server-authoritative, complementada por um sistema de RPG original com 1000 níveis, 30 raças, híbridos, habilidades e progressão infinita exclusiva para a raça Humana via "Evolução".

### Pilares Fundamentais:
1. **Server-Authoritative:** O cliente apenas envia intenções de ação (requests). O servidor valida permissões, recursos, cooldowns, integridade e executa as transações.
2. **Separação Rígida Client/Server:** Nenhum código ou biblioteca cliente (Screens, KeyMappings, Renderers, Minecraft.getInstance()) pode ser referenciado em caminhos comuns ou executados no Dedicated Server.
3. **Data-Driven por Design:** Estatísticas de raças, atributos, curvas de XP, drops, receitas e habilidades são definidas em arquivos de dados (JSON/Codecs), geradas por Data Generators e customizáveis por Data Packs.
4. **Preservação de Dados de Terraria:** Separação estrita entre `sourceStats` (valores nominais exatos de Terraria 1.4.5.8) e `runtimeStats` (valores convertidos para gameplay 3D).
5. **Anti-Cheat e Resiliência Numérica:** Proteção integral contra overflow, números negativos, exploits de duplicação, spoofing de pacotes e dessincronização de ticks.

---

## 2. Sistema RPG Original

### 2.1 Progressão de Nível e XP
- **Nível Inicial:** 1 | **Nível Máximo Normal:** 1000.
- **Pontos por Nível:** 5 Status Points concedidos no Nível 1 e a cada novo nível (totalizando 5000 pontos normais no nível 1000).
- **Curva de Experiência (TerraXP):** Totalmente desacoplada do XP vanilla.
  $$\text{requiredXP}(\text{level}) = \text{baseXP} \times \text{level}^{\text{exponent}} \times \text{raceXpMultiplier}$$
  Valores padrão: $\text{baseXP} = 100$, $\text{exponent} = 1.35$.
- **Status Points por Kills (`killPointProgress`):** Mobs concedem progresso fracionário de pontos de status baseado em Threat Rating (ex: 0.10, 0.25, 0.5, 1, 5, 20, 50). Ao completar 1.0, converte-se em 1 Status Point disponível. Bosses concedem bônus substanciais no primeiro abate e retornos decrescentes configuráveis em repetições.

### 2.2 Os 7 Atributos Primários
Existem exatamente 7 atributos distribuíveis pelo jogador:
1. **Defesa:** Redução/resistência a dano físico.
2. **Defesa Mágica:** Redução/resistência a dano mágico.
3. **Ataque:** Multiplicador de dano físico.
4. **Ataque Mágico:** Multiplicador de dano mágico.
5. **Crítico:** Multiplicador de dano crítico.
6. **Chance de Crítico:** Probabilidade percentual de acerto crítico (clamp normal de 100%).
7. **Velocidade:** Velocidade de movimento terrestre com retornos decrescentes e teto de segurança contra dessincronização de chunks.

*Nota:* Vida e Mana **não** são atributos distribuíveis pelo menu K; ambas evoluem exclusivamente via mecânicas de Terraria (Life Crystal, Life Fruit, Mana Crystals), raça e equipamentos.

### 2.3 Ranks e Caps de Atributos
- **Global Caps Base:** Defense (800), Magic Defense (800), Attack (1000), Magic Attack (1000), Critical Damage (600), Critical Chance (500), Speed (500).
- **Cap Racial Efetivo:** $\text{Cap}_{\text{final}} = \text{GlobalCap} \times \text{RaceCapMultiplier}$.
- O cap racial limita o investimento direto de pontos. Bônus de equipamentos, poções e buffs podem ultrapassar temporariamente os valores derivados.

### 2.4 A Raça Humana e a Habilidade "Evolução"
- Ao atingir o nível 1000, raças comuns atingem seu teto de nível e pontos: não ganham mais pontos por XP ou por kills.
- **Evolução:** Habilidade exclusiva do Humano (e de Híbridos com ascendência Humana).
  - O nível permanece visualmente 1000 (indicando Potencial Infinito: $\infty$).
  - Continua acumulando `killPointProgress` e Status Points indefinidamente por meio de abates de criaturas, mini-bosses e bosses.
  - Ignora os limites máximos normais de atributos, permitindo investimento contínuo e sem teto de rank.
  - Tipagem protegida contra overflow com clamps técnicos seguros para o motor de jogo.

### 2.5 Raças (30 Raças Principais)
1. Humano (Equilibrado, XP 1.00, Evolução, Potencial Infinito pós-1000).
2. Goblin (Stats baixos, XP 0.80, Engenho Goblin — sinergia com crafting/reforge).
3. Slime (Stats muito baixos, XP 0.78, Corpo Gelatinoso — amortecimento de queda e impacto).
4. Demônio (Stats muito altos, XP 1.50, Domínio Infernal, Voo Natural).
5. Anjo (Stats muito altos, XP 1.55, Graça Celestial, Voo Natural).
6. Elfo (Afinidade mágica, XP 1.05, Fluxo Arcano).
7. Anão (Defesa massiva, XP 0.95, Coração da Montanha).
8. Orc (Ataque físico elevado, XP 1.00, Fúria de Sangue).
9. Vampiro (Médio-alto, XP 1.20, Banquete Carmesim — lifesteal controlado).
10. Lobisomem (Médio, XP 1.05, Frenesi Lunar).
11. Dracônico (Stats altos, XP 1.40, Despertar Dracônico, Voo Natural).
12. Fada (Mobilidade/magia, XP 1.15, Salto Feérico, Voo Natural).
13. Harpia (Mobilidade aérea extrema, XP 1.10, Soberania dos Ventos, Voo Natural).
14. Tritão (Poder aquático, XP 0.95, Domínio das Marés).
15. Morto-Vivo (Resistência, XP 1.00, Negação da Morte).
16. Golem (Defesa massiva, velocidade baixa, XP 1.25, Fortaleza Viva).
17. Nascido das Sombras (Ataque/velocidade, XP 1.15, Passo Sombrio).
18. Nascido da Tempestade (Velocidade/raios, XP 1.15, Sobrecarga).
19. Fênix (Stats altos, XP 1.45, Renascimento, Voo Natural).
20. Nascido do Gelo (Defesa mágica, XP 1.00, Zero Absoluto).
21. Dríade (Regeneração/natureza, XP 0.95, Pacto da Natureza).
22. Homem-Fera (Ataque/predador, XP 0.95, Instinto Predador).
23. Enderiano (Teleporte/mobilidade, XP 1.20, Caminhante da Fenda).
24. Nascido do Vazio (Absorção de projéteis, XP 1.35, Devorar o Vazio).
25. Astral (Magia/crítico, XP 1.35, Surto Astral, Voo Natural).
26. Espectro (Eéreo/fantasmal, XP 1.20, Forma Etérea, Voo Natural).
27. Insectoide (Carapaça/metamorfose, XP 0.90, Metamorfose — voo temporário).
28. Titã (Força massiva, velocidade reduzida, XP 1.50, Colosso).
29. Aetheriano (Magia massiva, XP 1.45, Singularidade de Mana, Voo Natural).
30. Ceifador (Ataque/crítico, XP 1.30, Colheita de Almas).

### 2.6 Híbridos (1% Chance Padrão)
- Seleção de duas raças distintas.
- Stats Base = média das duas raças.
- Caps = média dos caps.
- XP Multiplier = média dos multiplicadores $+ 0.10$ (penalidade padrão).
- Recebe **ambas** as habilidades ativas/passivas.
- Voo concedido se pelo menos uma das raças tiver voo natural.

### 2.7 Voo e Asas de Fênix
- Raças com voo natural: Demônio, Anjo, Dracônico, Fada, Harpia, Fênix, Astral, Espectro, Aetheriano (e Insectoide em Metamorfose).
- Voo autorizado server-side estilo criativo sem consumo de stamina por padrão.
- **Asas de Fênix:** Artefato Especial craftável que concede voo criativo completo a raças terrestres quando equipado no slot especial.

### 2.8 Os 10 Special Accessories
Slot único no Menu K (`terraforge_rpg:special_accessories`):
1. Asas de Fênix (Voo estilo criativo).
2. Fragmento do Trovão (+40% Movement Speed terrestre e sprint).
3. Coração do Vazio (Teleporte curto controlado com cooldown).
4. Núcleo do Titã (Bônus maciço de dano físico e knockback resist).
5. Prisma Arcano (Aumento de dano mágico e regeneração de mana).
6. Selo do Guardião (Bônus conjunto de Defesa e Defesa Mágica).
7. Cristal de Sangue (Lifesteal controlado e anti-abuso).
8. Engrenagem do Tempo (Aceleração de use time e cooldowns não-raciais).
9. Olho do Predador (Bônus de Chance de Crítico e Dano Crítico).
10. Sigilo Gravitacional (Salto amplificado, controle aéreo e imunidade a dano de queda).

---

## 3. Integração Terraria

1. **Classes de Dano:** Melee, Ranged, Magic, Summoner. Definidas pelo item/arma em uso.
2. **Motor de Combate Centralizado:** Fluxo estrito de cálculo de dano, mitigação, crítico e efeitos on-hit.
3. **Acessórios Normais:** Inventário dedicado (`TerrariaAccessoryInventory`), independente do slot de Artefato Especial.
4. **Mundo e Camadas (Terraria Realm):** Dimensão dedicada `terraforge_rpg:terraria_realm` estruturada em camadas verticais contínuas (Space, Surface, Underground, Cavern, Underworld).
5. **Hardmode:** Estado global do mundo que altera spawns, libera minérios, desencadeia biomas (Hallow e World Evil) e desbloqueia novos tiers de chefes.
6. **Boss Framework:** Máquina de estados desacoplada, telegraphing claro em 3D, bosses segmentados (multi-part), scaling dinâmico multiplayer e barras de vida personalizadas.
7. **Bestiário, NPC Housing e Pesca:** Adaptação completa das mecânicas de Terraria respeitando a geometria cúbica do Minecraft.

---

## 4. Requisitos de Qualidade e Operação

1. **Integridade do Save:** Versionamento de dados com `DataFixers` / migração automática de NBT/Components.
2. **Networking Robusto:** Payloads com validação server-side contra spam, buffers truncados e overflows.
3. **Data Generation:** Todos os blocos, itens, tabelas de saque, tags e receitas devem ser mantidos via Datagen.
4. **Dedicated Server:** Proibição irrestrita de referências ao client em servidores dedicados.
5. **GameTests e Validação Automática:** Validação estática e testes unitários garantindo que IDs duplicados, receitas quebradas e raças sem habilidade sejam detectados no ciclo de compilação.
