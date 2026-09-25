# TerraForge RPG — Race Balance & Progression Design

Este documento documenta os parâmetros de equilíbrio, arquétipos e diretrizes numéricas para as **30 Raças** do mod.

---

## 1. Filosofia de Design e Desigualdade Intencional

Conforme estipulado no documento mestre:
- As raças **não são simétricas nem igualmente fortes no início**. A desigualdade inicial e o RNG de nascimento fazem parte intencional do gameplay.
- Raças com atributos iniciais muito fracos (como Goblin e Slime) possuem taxas de crescimento de XP aceleradas ($\times 0.80$, $\times 0.78$) e habilidades com utilidades estratégicas de sobrevivência ou economia.
- Raças místicas e ancestrais (como Demônio, Anjo, Titã, Aetheriano) começam com estatísticas maciças e voo natural, mas pagam um custo severo na lentidão de avanço de nível ($\times 1.40$ a $\times 1.55$ no XP necessário).
- **Humano:** O padrão áureo de equilíbrio ($1.00$ em tudo). Possui o status inicial mediano, sem voo natural. No entanto, sua habilidade **Evolução** é a única que remove todos os tetos de atributos e permite progressão infinita após o nível 1000.

---

## 2. Tabela de Referência das 30 Raças

| ID | Nome | Arquétipo Principal | Multiplicador XP | Voo Natural | Habilidade Racial |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `human` | Humano | Equilibrado / Potencial Ilimitado | 1.00 | Não | Evolução (Progressão Infinita pós-1000) |
| `goblin` | Goblin | Baixo / Engenharia & Crafting | 0.80 | Não | Engenho Goblin (Eficiência em Reforge/Craft) |
| `slime` | Slime | Muito Baixo / Amortecimento | 0.78 | Não | Corpo Gelatinoso (Absorção de Impacto & Pulo) |
| `demon` | Demônio | Ofensivo Alto / Fogo | 1.50 | Sim | Domínio Infernal (Aura e Boost de Dano) |
| `angel` | Anjo | Suporte & Mágico Alto | 1.55 | Sim | Graça Celestial (Cura e Proteção) |
| `elf` | Elfo | Mágico Arcano | 1.05 | Não | Fluxo Arcano (Eficiência e Recuperação de Mana) |
| `dwarf` | Anão | Defensivo Físico | 0.95 | Não | Coração da Montanha (Defesa no Solo & Antiknockback) |
| `orc` | Orc | Berseker Físico | 1.00 | Não | Fúria de Sangue (Dano escala com vida perdida) |
| `vampire` | Vampiro | Dano Médio-Alto / Lifesteal | 1.20 | Não | Banquete Carmesim (Vampirismo com Cooldown) |
| `werewolf` | Lobisomem | Híbrido Físico / Noturno | 1.05 | Não | Frenesi Lunar (Poder sob Luar) |
| `draconic` | Dracônico | Bruiser Alado / Fogo | 1.40 | Sim | Despertar Dracônico (Sopro Elemental) |
| `fairy` | Fada | Hipermobilidade Mágica | 1.15 | Sim | Salto Feérico (Blink Curto com Efeitos) |
| `harpy` | Harpia | Velocidade Aérea Extrema | 1.10 | Sim | Soberania dos Ventos (Dash Aéreo & Rajada) |
| `triton` | Tritão | Poder Aquático | 0.95 | Não | Domínio das Marés (Maestria Aquática) |
| `undead` | Morto-Vivo | Resistência & Sobrevivência | 1.00 | Não | Negação da Morte (Cheat Death com Long Cooldown) |
| `golem` | Golem | Tanque Maciço / Baixa Velocidade | 1.25 | Não | Fortaleza Viva (Resistência Imensa Estática) |
| `shadowborn`| Nascido das Sombras | Assassino / Crítico Físico | 1.15 | Não | Passo Sombrio (Teleporte em Sombras) |
| `stormborn` | Nascido da Tempestade | Velocidade & Eletricidade | 1.15 | Não | Sobrecarga (Chain Lightning em Movimento) |
| `phoenix` | Fênix | Fogo / Ressurreição | 1.45 | Sim | Renascimento (Ressuscita com Explosão de Fogo) |
| `frostborn` | Nascido do Gelo | Defensivo Mágico / Gelo | 1.00 | Não | Zero Absoluto (Aura Congelante em Área) |
| `dryad` | Dríade | Regeneração & Controle | 0.95 | Não | Pacto da Natureza (Raízes & Cura Contínua) |
| `beastman` | Homem-Fera | Caçador Físico | 0.95 | Não | Instinto Predador (Bônus contra Alvos Feridos) |
| `enderian` | Enderiano | Mobilidade Espacial | 1.20 | Não | Caminhante da Fenda (Teleporte Direcionado) |
| `voidborn` | Nascido do Vazio | Anti-Projéteis / Vazio | 1.35 | Não | Devorar o Vazio (Absorção de Projéteis) |
| `astral` | Astral | Magia Cósmica / Crítico | 1.35 | Sim | Surto Astral (Chuva Estelar Ofensiva) |
| `spectre` | Espectro | Intangibilidade / Mobilidade | 1.20 | Sim | Forma Etérea (Faseamento de Entidades) |
| `insectoid` | Insectoide | Carapaça Crescente | 0.90 | Não (Metamorfose) | Metamorfose (Transformação com Asas e Carapaça) |
| `titan` | Titã | Colosso Físico | 1.50 | Não | Colosso (Crescimento de Escala & Poder Físico) |
| `aetherian` | Aetheriano | Hipermagia | 1.45 | Sim | Singularidade de Mana (Custo Zero Temporário) |
| `reaper` | Ceifador | Dano de Execução | 1.30 | Não | Colheita de Almas (Almas Geram Buff Ofensivo) |

---

## 3. Regras de Híbridos (1% de Ocorrência)
1. **Atributos Base:** $\text{Base}_{\text{Híbrido}} = \frac{\text{Base}_A + \text{Base}_B}{2}$.
2. **Caps de Atributos:** $\text{Cap}_{\text{Híbrido}} = \frac{\text{Cap}_A + \text{Cap}_B}{2}$.
3. **Multiplicador de XP:** $\text{XP}_{\text{Híbrido}} = \frac{\text{XP}_A + \text{XP}_B}{2} + 0.10$ (penalidade padrão configurável).
4. **Voo:** Concedido se $\text{Voo}_A \lor \text{Voo}_B = \text{true}$.
5. **Habilidades:** Recebe as duas habilidades completas vinculadas a Keybinds separadas (R e V).
6. **Evolução em Híbrido:** Se uma das raças for `human`, a habilidade Evolução entra em vigor sem qualquer restrição, concedendo progressão infinita após o nível 1000.
