# Guia Completo de Progressão: TerraForge RPG

**Versão de Referência Canônica:** Terraria PC 1.4.5.8  
**Mod Loader:** NeoForge para Minecraft 1.21.1 | **Java:** 21 LTS  
**Package:** `com.terraforge.rpg` | **Mod ID:** `terraforge_rpg`

---

## 1. Visão Geral da Progressão

TerraForge RPG integra a progressão canônica do Terraria 1.4.5.8 com mecânicas de RPG multiplayer modernas em um mundo tridimensional do Minecraft. O jogo é dividido em dois grandes arcos de progressão: **Pre-Hardmode** e **Hardmode (com Endgame)**.

---

## 2. Início de Jogo & Fundação RPG

### 2.1 Criação do Personagem e Raças
- Ao entrar pela primeira vez no mundo, cada jogador recebe aleatoriamente uma das **30 Raças Canônicas** (ex: *Humano, Elfo, Vampiro, Tritão, Draconiano, Titã, Autômato, Espírito das Estrelas, etc.*).
- Há **1% de probabilidade** de o jogador despertar como um **Híbrido**, herdando características genéticas de duas linhagens.
- Cada raça confere atributos base exclusivos e uma habilidade ativa (tecla padrão: `V`).

### 2.2 Sistema de Níveis & Atributos
- **Limite Máximo:** Nível 1000.
- Ao derrotar inimigos e minerar, ganha-se XP baseado no **Threat Rating** do alvo (`WEAK_MOB`, `COMMON`, `STRONG`, `ELITE`, `MINIBOSS`, `BOSS`, `EVENT_BOSS`).
- A cada nível conquistado, o jogador recebe **3 Pontos de Status** para alocar entre os 7 atributos primários:
  1. **Vigor (VIG):** Vida máxima (+5 HP/ponto), defesa natural e imunidades.
  2. **Mana (MAN):** Mana máxima (+5 MP/ponto), regeneração acelerada de feitiços.
  3. **Força (STR):** Dano Melee (+1.5%/ponto) e knockback aumentado.
  4. **Destreza (DEX):** Dano Ranged (+1.5%/ponto) e velocidade de movimento.
  5. **Inteligência (INT):** Dano Magic (+1.5%/ponto) e redução de custo de mana.
  6. **Afinidade (AFF):** Dano Summon (+1.5%/ponto) e capacidade de lacaios.
  7. **Sorte (LCK):** Chance de crítico (+0.2%/ponto), poder de pesca e taxas de drop.
- **Limites (Caps):** Soft Cap em 250 pontos (50% eficiência subsequente) e Hard Cap em 500 pontos por atributo.

### 2.3 Cristais de Vida e de Mana
- **Life Crystals:** Aumentam a vida máxima do jogador de 100 HP para 400 HP (+20 HP por cristal, até 15 cristais).
- **Life Fruits (Hardmode):** Aumentam a vida máxima de 400 HP para 500 HP (+5 HP por fruto dourado, até 20 frutos).
- **Mana Crystals:** Aumentam a reserva máxima de mana de 20 MP para 200 MP (+20 MP por cristal estelar, até 9 cristais).

---

## 3. Pre-Hardmode: Da Superfície ao Submundo

### 3.1 Camadas do Terraria Realm
- **Space (Espaço):** $Y \ge 260$ (Gravidade reduzida, Wyverns e Harpias).
- **Surface (Superfície):** $Y \in [120, 259]$ (Florestas, Desertos, Taigas, Oceanos).
- **Underground (Subterrâneo):** $Y \in [40, 119]$ (Cavernas rasas, minérios de cobre, ferro, ouro).
- **Cavern (Cavernas Profundas):** $Y \in [-30, 39]$ (Gemas, teias, lava, Life Crystals).
- **Underworld (Submundo):** $Y < -30$ (Cinzas, forjas do inferno, Hellstone e lava contínua).

### 3.2 Bosses do Pre-Hardmode
1. **King Slime:** Invocado com o `Slime Crown` ou através do evento `Slime Rain` (150 slimes eliminados). Concede a `Slimy Saddle` (montaria com super salto e dano de esmagamento).
2. **Eye of Cthulhu:** Invocado com o `Suspicious Looking Eye` à noite. Fase 1: servos e vôo; Fase 2 ($\le 50\%$ HP): dentes à mostra, 0 de defesa, avanços ultrarrápidos.
3. **Eater of Worlds / Brain of Cthulhu:** Convocados nas profundezas do bioma maligno (Corruption/Crimson) ao quebrar Shadow Orbs ou Crimson Hearts. Concedem Shadow Armor ou Crimson Armor.
4. **Skeletron:** Derrotado na entrada do Dungeon para libertar a maldição do Velho e garantir acesso irrestrito ao Dungeon Pre-Hardmode (onde se encontram Water Bolt, Muramasa e Chaves de Ouro).
5. **Wall of Flesh (A Parede de Carne):**
   - **Invocação:** Jogar o `Guide Voodoo Doll` na lava do Submundo enquanto o Guia estiver vivo.
   - **Combate:** 8.000 HP, 12 de defesa, avança horizontalmente varrendo o Submundo. Fase 2 com rajadas de laser e The Hungry.
   - **Drops:** `Pwnhammer` (80% hammer power) e Emblemas de Classe (`Warrior Emblem`, `Ranger Emblem`, `Sorcerer Emblem`, `Summoner Emblem`).
   - **Transição:** Sua derrota ativa permanentemente o **Hardmode** mundial!

---

## 4. Hardmode: O Despertar da Luz e da Escuridão

### 4.1 Altares e Novos Minérios
- Ao quebrar Altares Demoníacos/Carmesins com o `Pwnhammer`:
  - **1º Altar:** Abençoa o mundo com *Cobalt* ou *Palladium*.
  - **2º Altar:** Abençoa o mundo com *Mythril* ou *Orichalcum*.
  - **3º Altar:** Abençoa o mundo com *Adamantite* ou *Titanium*.

### 4.2 Os Três Bosses Mecânicos
Invocados durante a noite:
1. **The Twins (Retinazer & Spazmatism):**
   - Invocador: `Mechanical Eye`.
   - Retinazer: 24.000 HP. Fase 2: canhão de laser contínuo.
   - Spazmatism: 23.000 HP. Fase 2: lança-chamas amaldiçoado e 5 investidas violentas.
   - Drops: `Soul of Sight` e `Hallowed Bar`.
2. **The Destroyer:**
   - Invocador: `Mechanical Worm`.
   - 80.000 HP. Minhoca mecânica colossal que solta `Destroyer Probes` (200 HP) e dispara lasers radiais de seus segmentos.
   - Drops: `Soul of Might` e `Hallowed Bar`.
3. **Skeletron Prime:**
   - Invocador: `Mechanical Skull`.
   - 28.000 HP. Cabeça giratória (48 de defesa em giro) e 4 membros (Cannon, Saw, Vice, Laser). Ao amanhecer, entra no modo Dungeon Guardian (9999 defesa, 1000 de dano).
   - Drops: `Soul of Fright` e `Hallowed Bar`.

*Ao derrotar todos os 3 chefes mecânicos, a mensagem icônica é transmitida: "The jungle grows restless...", liberando os Life Fruits e os Bulbos da Plantera na Selva Subterrânea!*

---

## 5. Hardmode Avançado & Endgame Cósmico

### 5.1 Plantera
- **Invocação:** Quebrar um Plantera's Bulb na Selva Subterrânea.
- **Estatísticas:** 30.000 HP, 36 de defesa (Fase 1) / 10 de defesa (Fase 2).
- **Mecânicas:** Dispara sementes venenosas e espinhos. Na Fase 2 ($\le 50\%$ HP), corre na direção do jogador como uma boca carnívora rápida.
- **Enfurecimento:** Fica enfurecida com 72 de defesa e velocidade absurda se for atraída para fora da Selva Subterrânea.
- **Drops:** `Temple Key` (abre a porta do Templo Lihzahrd).

### 5.2 Golem
- **Invocação:** Usar `Lihzahrd Power Cell` no Altar de Lihzahrd no coração do Templo.
- **Estatísticas:** 16.000 HP (cabeça) + corpo de pedra (32 de defesa).
- **Mecânicas:** Saltos pesados, bolas de fogo e socos mecânicos. Na Fase 2, a cabeça flutua disparando lasers de alta voltagem.
- **Drops:** `Beetle Husk` (para a poderosa Beetle Armor).

### 5.3 Duke Fishron
- **Invocação:** Pescar no Oceano utilizando um `Truffle Worm` (666% de Poder de Isca).
- **Estatísticas:** 60.000 HP, 50 de defesa.
- **Mecânicas:** 5 investidas supersônicas, bolhas detonantes e tornados marítimos (*Sharknados / Cthulhunados*).
- **Drops:** Arco `Tsunami` (dispara 5 flechas paralelas ao custo de 1).

### 5.4 Moon Lord (O Conquistador Celestial)
- **Invocação:** Usar o `Celestial Sigil`.
- **Estatísticas:** 145.000 HP total.
- **Mecânicas:**
  - *Phantasmal Deathray:* Raio mortal cósmico varrendo o céu (150+ de dano).
  - *Phantasmal Bolts & Spheres:* Projéteis cósmicos teleguiados.
  - *True Eyes of Cthulhu:* Olhos orbitais libertados na segunda metade da batalha.
- **Drops:**
  - Espada lendária `Meowmere` (200 de dano base + gatos arco-íris saltitantes).
  - Minério cósmico `Luminite Ore` (para barras de Luminite e armaduras supremas).
  - Ferramenta cósmica `Portal Gun`.

---

## 6. Vilas, Moradias e NPCs 3D

Para atrair Town NPCs (Guia, Mercador, Enfermeira, Goblin Tinkerer):
- **Dimensões:** Quarto fechado com área entre 30 e 750 blocos cúbicos.
- **Conforto:** 1 fonte de luz (tocha/vela), 1 mesa/superfície plana, 1 cadeira/cama, porta e piso sólido.
- **Serviços:** O Goblin Tinkerer permite reforjar armas e acessórios com prefixos canônicos (como *Legendary, Unreal, Mythical, Godly, Warding, Menacing*).

---

## 7. Comandos Úteis do TerraForge RPG

- `/rpg menu`: Abre o menu de status e habilidades.
- `/rpg reforge`: Reforja o item segurado pelo custo de moedas correspondente.
- `/rpg progression`: Consulta o status atual do mundo (Hardmode, chefes derrotados, eventos).
- `/rpg setlevel <jogador> <nível>`: Define o nível do jogador (Administradores).
- `/rpg hardmode <true|false>`: Ativa ou desativa o Hardmode manualmente.
