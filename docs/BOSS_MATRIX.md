# TerraForge RPG — Boss Matrix & Behavioral Architecture

Este documento cataloga todos os chefes de Terraria 1.4.5.8 a serem transpostos para o mod, suas fases comportamentais e adaptações tridimensionais.

---

## 1. Diretrizes de IA em Três Dimensões

- Nenhum chefe será implementado como um "zumbi gigante com muita vida".
- Cada boss preserva sua identidade visual, silhueta e padrões de ataque originais de Terraria adaptados ao espaço 3D (ex: vetores de investida, órbitas esféricas em torno do jogador, altitude relativa e telegrafia de projéteis).
- **Segmented Bosses:** Framework próprio para criaturas segmentadas (Eater of Worlds, The Destroyer), com propagação de dano, colisão articulada e interpolação de elos.

---

## 2. Catálogo de Chefes Principais (1.4.5.8)

| Boss | Tier / Era | Comportamento 3D Principal | Fases | Segmentado |
| :--- | :--- | :--- | :--- | :--- |
| **King Slime** | Early Pre-Hardmode | Salto parabólico massivo, teleporte por proximidade, spawn de slimes menores | 2 Fases (Enrage com HP baixo) | Não |
| **Eye of Cthulhu** | Early Pre-Hardmode | Órbita aérea circular, investidas angulares 3D, invocação de Servos | 2 Fases (Transformação de mandíbula) | Não |
| **Eater of Worlds** | Mid Pre-Hardmode | Escavação subterrânea 3D, ataques vindos do solo, divisão de segmentos | N segmentos independentes | Sim |
| **Brain of Cthulhu** | Mid Pre-Hardmode | Teleportes com clones ilusórios e névoa 3D em torno da arena | 2 Fases (Creeper swarm + Rush) | Não (Multipart) |
| **Queen Bee** | Mid Pre-Hardmode | Voo em alta velocidade, investidas horizontais rasantes e enxame de ferrões | Contínua (Velocidade escala com HP) | Não |
| **Skeletron** | Late Pre-Hardmode | Cabeça voadora com braços articulados independentes em cinemática inversa | 2 Fases (Perda de braços libera projéteis) | Sim (Multipart) |
| **Deerclops** | Pre-Hardmode | Ataques de gelo em ondas de choque terrestres, debuff de frio e mãos sombrias | 2 Fases | Não |
| **Wall of Flesh** | Fim do Pre-Hardmode | Muralha maciça de carne varrendo a largura do Underworld, empurrando o jogador | Fases escalonadas por velocidade | Sim (Multipart) |
| **Queen Slime** | Early Hardmode | Saltos explosivos, projéteis ricocheteantes e asas com voo ativo na Fase 2 | 2 Fases (Voo ativo) | Não |
| **The Destroyer** | Hardmode Mecânico | Minhoca mecânica titânica, sondas laser destacáveis (Probes) | Compartilhada com Probes | Sim |
| **The Twins** | Hardmode Mecânico | Duas entidades interdependentes (Retinazer e Spazmatism) com IAs coordenadas | 2 Fases para cada olho | Não (Dual Entity) |
| **Skeletron Prime** | Hardmode Mecânico | 4 braços mecânicos simultâneos (Canhão, Serra, Laser, Vice) e giro com telegrafia | Multi-part dinâmico | Sim (Multipart) |
| **Plantera** | Mid Hardmode | Movimentação baseada em ganchos no teto da selva, tentáculos e esporos | 2 Fases (Desabrochar da carapaça) | Sim (Multipart) |
| **Golem** | Mid Hardmode | Corpo em plataforma, punhos disparáveis extensíveis, cabeça laser destacável | 2 Fases (Cabeça voadora livre) | Sim (Multipart) |
| **Duke Fishron** | Late Hardmode | Investidas de altíssima velocidade em 3 eixos, tufões de água e Sharknados | 3 Fases (Fase invisível no Expert/Master) | Não |
| **Empress of Light** | Late Hardmode | Padrões de projéteis luminosos e raios com telegrafia geométrica (Bullet Hell 3D) | 2 Fases (Insta-kill à luz do dia) | Não |
| **Lunatic Cultist** | Endgame | Teleportes aéreos rápidos, clones ilusórios com ritual e feitiços arcanos | Ciclos de rituais | Não |
| **Moon Lord** | Boss Final | Mãos colossais, olho da testa, raio laser Phantasmal com rotação 3D e True Eyes | 2 Fases (Abertura do núcleo torácico) | Sim (Multipart) |
