# TerraForge RPG — Centralized Combat Engine Formulas

Este documento define o pipeline unificado de cálculo de dano, conversão de ranks dos 7 atributos e mitigação de dano entre atacante e defensor.

---

## 1. Pipeline Central de Resolução de Dano

O cálculo de dano é estritamente **server-side** e obedece ao seguinte fluxo sequencial:

```
[Base Weapon Damage (sourceStats)]
   ↓
[Terraria Modifiers / Prefixes]
   ↓
[Player RPG Stat Multiplier (Attack / Magic Attack Rank)]
   ↓
[Equipment & Armor Set Multipliers]
   ↓
[Active Buffs / Debuffs]
   ↓
[Racial Trait Multipliers]
   ↓
[Special Accessory Multipliers]
   ↓
[Critical Roll (Critical Chance Rank)]
   ↓
[Critical Multiplier (Critical Rank)]
   ↓
[Target Defenses (Defense / Magic Defense Rank)]
   ↓
[Target Resistances & Difficulty Scaling]
   ↓
[Final Damage Clamping (Mínimo de 1 de dano)]
   ↓
[On-Hit Effects, Lifesteal & Secondary Events]
```

---

## 2. Conversão de Ranks para Gameplay

Para permitir 1000 níveis sem desintegrar o balanceamento inicial, os valores distribuídos no Menu K operam como **Ranks**, convertidos por fórmulas centralizadas em `AttributeCalculator`:

### 2.1 Ataque Físico
$$\text{PhysicalMultiplier} = 1.0 + (\text{AttackRank} \times 0.0025)$$
*Exemplo:* 400 Ranks conferem $+100\%$ de dano físico base.

### 2.2 Ataque Mágico
$$\text{MagicMultiplier} = 1.0 + (\text{MagicAttackRank} \times 0.0030)$$
*Exemplo:* 500 Ranks conferem $+150\%$ de dano mágico base.

### 2.3 Defesa Física
$$\text{PhysicalReduction} = \frac{\text{DefenseRank}}{\text{DefenseRank} + 400}$$
*Comportamento assintótico:* 400 Ranks = $50\%$ de redução; 800 Ranks = $66.6\%$ de redução.

### 2.4 Defesa Mágica
$$\text{MagicReduction} = \frac{\text{MagicDefenseRank}}{\text{MagicDefenseRank} + 400}$$
*Comportamento idêntico aplicado especificamente contra fontes de dano mágico.*

### 2.5 Chance de Crítico
$$\text{CritChanceTotal} = \text{BaseWeaponCrit} + (\text{CritChanceRank} \times 0.15)\%$$
*Clamp:* Hard cap padrão de $100\%$ (salvo se `overcrit` for ativado em configuração do servidor).

### 2.6 Dano Crítico
$$\text{CritMultiplier} = 1.5 + (\text{CritDamageRank} \times 0.002)$$
*Exemplo:* 500 Ranks elevam o multiplicador crítico de $1.5\times$ para $2.5\times$.

### 2.7 Velocidade de Movimento (Speed)
$$\text{MovementSpeedBonus} = \min\left(0.60, \frac{\text{SpeedRank}}{\text{SpeedRank} + 500} \times 0.80\right)$$
*Segurança Técnica:* Previne que velocidades hiperbólicas quebrem o carregamento de chunks e o motor de colisão do Minecraft, mesmo para a raça Humana com Evolução.
