# TerraForge RPG — Project Roadmap

Este documento define o plano mestre de execução e o status atual de cada uma das 27 fases do projeto (Fase 0 até Fase 26).

---

## Índice das Fases

- [x] **Fase 0: Foundation** — Configuração do projeto, NeoForge 1.21.1, Java 21, documentação mestre, registries base, skeleton datagen, logging, validação e build verde.
- [x] **Fase 1: Player Data & Attachments** — Sistema de dados persistentes do jogador, versionamento de save, ciclo de vida, sync networking e segurança server-authoritative.
- [x] **Fase 2: Level & Experience (TerraXP)** — Níveis 1 a 1000, curvas de XP modular, 5 pontos por nível, `killPointProgress`, Threat Ratings e recompensas de abate.
- [x] **Fase 3: 7 Atributos & Caps** — Defesa, Defesa Mágica, Ataque, Ataque Mágico, Crítico, Chance de Crítico e Velocidade. Calculadoras de rank, caps globais e investimento server-side.
- [x] **Fase 4: Menu do Personagem (Tecla K)** — Interface RPG completa, modelo 3D do jogador, exibição detalhada de stats, botões de distribuição (+1, +5, +10, MAX) e sincronização.
- [x] **Fase 5: Raças (30 Raças)** — Sistema data-driven de raças, sorteio no primeiro login, stats base, multiplicadores de cap, multiplicadores de XP e voo natural.
- [x] **Fase 6: Sistema de Híbridos (1% Chance)** — Seleção de duas raças, unificação de habilidades, médias de stats/caps, penalidade de XP e persistência.
- [x] **Fase 7: Racial Abilities Framework** — API de habilidades (ativa/passiva/toggle), 30 habilidades implementadas, keybinds (R e V), cooldowns e partículas.
- [x] **Fase 8: Special Accessories (10 Artefatos)** — Slot dedicado, seletor de artefatos no menu K, Asas de Fênix, Fragmento do Trovão e os outros 8 artefatos.
- [x] **Fase 9: Health, Mana & HUD** — Progressão de vida (Life Crystal/Fruit), mana própria (cristais, consumo e regeneração) e HUD customizado configurável.
- [x] **Fase 10: Combat Engine Centralizado** — Pipeline completo de dano: physical, magic, defense calculation, critical roll, mitigação e efeitos on-hit.
- [x] **Fase 11: Item Framework & Modifiers/Reforging** — Classes base de itens, raridades de Terraria, prefixos/modifiers de equipamento e mecânica de reforge no NPC/estação.
- [x] **Fase 12: Weapon Frameworks & Projectiles** — Melee (espadas, lanças, flails, bumerangues, ioiôs com Yoyo API), Ranged (arcos, armas de fogo, munições), Magic e Summoner (minions/sentries).
- [ ] **Fase 13: Mob Framework & Threat Rating** — IA de monstros adaptada ao 3D, taxas de spawn, loot tables e integração de drops/XP.
- [ ] **Fase 14: Boss Framework & Multipart/Scaling** — Máquina de estados para bosses, fases baseadas em HP, telegrafia 3D, bosses segmentados e scaling multiplayer.
- [ ] **Fase 15: Boss de Prova de Conceito (King Slime / Eye of Cthulhu)** — Implementação completa de ponta a ponta: IA 3D, modelo, animações, estados, ataques, loot bag, relic e avanço de progressão.
- [ ] **Fase 16: Geração de Mundo & Terraria Realm** — Dimensão `terraria_realm`, geração vertical em camadas (Space, Surface, Underground, Cavern, Underworld) e biomas essenciais.
- [ ] **Fase 17: Hardmode & Transformação de Mundo** — Ativação de Hardmode via derrota do Wall of Flesh, biomas de Hallow e World Evil (Corruption/Crimson) com taxa de dispersão otimizada.
- [ ] **Fase 18: Framework de Eventos & Invasões** — Slime Rain, Blood Moon, Goblin Army, Solar Eclipse, Lunar Events com barras de progresso e ondas de inimigos.
- [ ] **Fase 19: Town NPCs & 3D Housing** — Validador 3D de moradia, rotinas dos NPCs, lojas, felicidade de bioma e progressão de resgate.
- [ ] **Fase 20: Pesca, Utilitários & Montarias** — Sistema de pesca Terraria-like (iscas, crates, biomas), grappling hooks, montarias e pets/light pets.
- [ ] **Fase 21: Importação de Conteúdo em Lotes (Pre-Hardmode)** — Lotes controlados de itens, blocos, minérios, armaduras e receitas.
- [ ] **Fase 22: Implementação de Todos os Bosses (Terraria 1.4.5.8)** — Todos os bosses da matriz de conteúdo com fases e comportamentos completos.
- [ ] **Fase 23: Implementação de Todos os Mobs** — Todos os inimigos, variantes e criaturas passivas da versão canônica.
- [ ] **Fase 24: Implementação de Todos os Itens e Blocos** — Conclusão do catálogo de armaduras, acessórios, ferramentas, blocos e consumíveis.
- [ ] **Fase 25: Polimento, Áudio, Efeitos & Performance** — Partículas client-side, SFX e música integrados, benchmarking de ticks e otimização de renderização.
- [ ] **Fase 26: Validação Final, GameTests & Lançamento 1.0** — 100% de cobertura da CONTENT_MATRIX, testes em servidor dedicado multiplayer e suite de GameTests aprovada.
