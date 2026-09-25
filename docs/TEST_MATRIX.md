# TerraForge RPG — Test Matrix & Verification Protocols

Este documento registra a matriz de testes automatizados e critérios de aceitação para os sistemas centrais do mod (conforme Seção 143 do Master Spec).

---

## 1. Matriz de Verificação de Sistemas

| ID | Caso de Teste | Critério de Aceitação | Status |
| :--- | :--- | :--- | :--- |
| **TEST-01** | Inicialização de Raça | Novo jogador sem dados recebe exatamente 1 raça válida sorteada entre as 30 | PLANEJADO |
| **TEST-02** | Persistência de Raça | A raça do jogador permanece idêntica após reconexão, teleporte e morte | PLANEJADO |
| **TEST-03** | Rolo de Híbrido | Híbrido possui flag ativa e duas raças estritamente distintas | PLANEJADO |
| **TEST-04** | Habilidades Híbridas | Híbrido recebe e executa ambas as habilidades primária e secundária | PLANEJADO |
| **TEST-05** | Level Inicial & Pontos | Personagem inicia no Nível 1 com 5 Status Points disponíveis | PLANEJADO |
| **TEST-06** | Progressão de Level | Cada nível novo concede exatamente +5 Status Points | PLANEJADO |
| **TEST-07** | Teto de Nível 1000 | Nível não ultrapassa 1000 sob nenhuma circunstância de XP | PLANEJADO |
| **TEST-08** | Cap de Pontos (Sem Evolução)| Raças comuns no nível 1000 não acumulam novos pontos por XP ou kills | PLANEJADO |
| **TEST-09** | Humano com Evolução | Humano no nível 1000 continua ganhando pontos via kills indefinidamente | PLANEJADO |
| **TEST-10** | Híbrido com Humano | Híbrido contendo ascendência humana recebe integralmente a Evolução | PLANEJADO |
| **TEST-11** | Validação de Caps | Investimento de pontos é bloqueado ao atingir o cap racial configurado | PLANEJADO |
| **TEST-12** | Evolução Pós-1000 Caps | Humano pós-1000 ignora tetos máximos normais de ranks | PLANEJADO |
| **TEST-13** | Integridade de Pontos | Pontos disponíveis e investidos nunca assumem valores negativos | PLANEJADO |
| **TEST-14** | Rejeição de Pacotes | Servidor rejeita pacotes com valores negativos, spoofing ou índices falsos | PLANEJADO |
| **TEST-15** | Slot de Special Accessory | Apenas 1 Special Accessory pode ser equipado simultaneamente | PLANEJADO |
| **TEST-16** | Anti-Duplicação | Equipar/desequipar artefatos especiais não duplica itens no inventário | PLANEJADO |
| **TEST-17** | Voo das Asas de Fênix | Equipar Asas de Fênix concede voo criativo a raças terrestres | PLANEJADO |
| **TEST-18** | Remoção de Asas de Fênix | Desequipar remove o voo imediatamente se a raça não for alada | PLANEJADO |
| **TEST-19** | Voador Natural Sem Asas | Raças aladas mantêm o voo mesmo sem as Asas de Fênix | PLANEJADO |
| **TEST-20** | Fragmento do Trovão | Equipar artefato confere exatamente o bônus de velocidade configurado (+40%) | PLANEJADO |
| **TEST-21** | Cooldown de Habilidades | Habilidades raciais respeitam estritamente a duração de cooldown | PLANEJADO |
| **TEST-22** | Persistência Pós-Morte | Nível, pontos, raça e atributos não são resetados após a morte | PLANEJADO |
| **TEST-23** | Fórmula de Dano Físico | Combat engine calcula dano físico aplicando multiplicadores e mitigação | PLANEJADO |
| **TEST-24** | Fórmula de Dano Mágico | Dano mágico é mitigado exclusivamente pela Defesa Mágica do alvo | PLANEJADO |
| **TEST-25** | Rolo de Crítico | Acertos críticos ocorrem conforme a probabilidade e aplicam o multiplicador | PLANEJADO |
| **TEST-26** | Progressão de Mundo | Derrota de bosses atualiza flags em `WorldProgressionData` no servidor | PLANEJADO |
| **TEST-27** | Ativação do Hardmode | Derrota do Wall of Flesh altera o estado global do mundo para Hardmode | PLANEJADO |
| **TEST-28** | Dedicated Server Compatibility | O mod inicia e executa em servidor dedicado sem erros de classes de cliente | PLANEJADO |
