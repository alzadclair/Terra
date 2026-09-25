# Terraria Reference Version

**Target Version:** Terraria PC 1.4.5.8  
**Release Channel:** PC / Steam Standard Release  
**Document Status:** LOCKED REFERENCE BASELINE  

---

## 1. Regra de Integridade da Versão

1. Todas as estatísticas numéricas (dano base, velocidade, knockback, uso de mana, vida, defesa, drops, chances, taxas de spawn) devem refletir exclusivamente **Terraria PC 1.4.5.8**.
2. É terminantemente proibido misturar silenciosamente dados de versões anteriores (ex: 1.4.4.9, 1.4.3, 1.3.5) ou de versões mobile/console que possuam mecânicas ou números divergentes.
3. Caso uma mecânica ou item sofra alteração na 1.4.5.8 em relação a versões anteriores, a versão 1.4.5.8 é a verdade canônica.
4. Se determinado dado exato for inacessível ou estiver pendente de validação nos arquivos da 1.4.5.8, deve ser categorizado como `BLOCKED_DATA` na matriz de conteúdo (`docs/CONTENT_MATRIX.csv`), nunca preenchido com valores arbitrários de memória.

---

## 2. Escopo de Conteúdo da 1.4.5.8

A versão cobre:
- Todo o ciclo Pre-Hardmode e Hardmode.
- Todas as armas (Melee, Ranged, Magic, Summoner).
- Todas as armaduras, conjuntos e vanity.
- Todos os acessórios, incluindo wings, balões, botas e combinações do Tinkerer's Workshop.
- Todos os bosses, mini-bosses e invasões.
- Biomas e sub-biomas, incluindo Aether / Shimmer e transformações associadas.
- Mecânicas de pesca, clima, moon phases e ciclo dia/noite.
- Interações de NPCs, Housing e Bestiary.

---

## 3. Registro de Divergências e Adaptações 3D

Quando uma mecânica bidimensional de Terraria for transposta para o ambiente 3D do Minecraft:
- A adaptação matemática e geométrica deve ser documentada explicitamente em `docs/DECISIONS.md`.
- As estatísticas nominais puras (`sourceStats`) são preservadas intactas, e a conversão de escala 3D (`runtimeStats`) é aplicada exclusivamente por adaptadores centralizados (`TerrariaStatConverter`).
