# TerraForge RPG — Known Issues & Technical Debt Tracker

Este documento registra limitações temporárias conhecidas, pendências de assets e notas técnicas em monitoramento ativo.

---

## 1. Problemas e Limitações Conhecidas

| ID | Componente | Descrição | Workaround / Ação | Status |
| :--- | :--- | :--- | :--- | :--- |
| **KI-001** | Toolchain | Build inicial do NeoForge necessita de cache local inicial de dependências | Gradle Wrapper 8.14.3 e cache do repositório Alza Adventures reutilizados com sucesso | RESOLVIDO |
| **KI-002** | Assets Artísticos | Modelos Blockbench e texturas finais aguardando integração nas fases dedicadas | Placeholders válidos e geradores de geometria procedural utilizados nas fases iniciais | ACOMPANHANDO |
| **KI-003** | Áudio Proprietário | Proibição de inclusão direta de arquivos de áudio extraídos de Terraria | Sistema de SoundEvents mapeado com placeholders compatíveis para posterior substituição legal | DOCUMENTADO |
