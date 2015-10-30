# Arquitetura de Software

<a name="index"/>
## Índice
1. [Introdução](#introduction)
2. [Vista Lógica](#logicalview)
3. [Vista de Implementação](#implementationview)
4. [Vista de Processo](#processview)
5. [Vista de Distribuição](#deploymentview)
6. [Contribuição do Grupo](#group_contribution)

<a name="introduction"/>
## Introdução

No *Terasology* a arquitetura do software é muito importante pois tem de suportar o desenvolvimento paralelo de módulos diferentes a ritmos diferentes.
Para isso, os responsáveis pelo projeto publicaram vários documentos e *threads* no seu fórum que permitem aos *developers* externos ter uma noção de como as coisas têm de funcionar para garantir que o jogo funciona minimamente bem por muito maus ou incompletos que estejam os módulos. [Nesta *thread*](http://forum.terasology.org/threads/architecture-vision.690/) do fórum do *Terasology* são elicitados os objetivos da arquitetura implementada (embora esta não seja uma implementação muito forte/rígida). É de salientar que os módulos não devem causar conflitos entre eles, não podem causar falhas no jogo completo (por exemplo devido a exceções por "tratar") e deve haver uma clara separação da implementação dos módulos e do *engine* em si.

É detalhada mais profundamente [nesta página](https://github.com/MovingBlocks/Terasology/wiki/Entity-System-Architecture) a arquitetura do sistema de entidades do jogo que é importante para programar módulos ou *resources* do jogo. Para além disso, [nesta página](https://github.com/MovingBlocks/Terasology/wiki/Codebase-Structure) é explicada em detalhe a importação do código dos diferentes módulos através da tecnologia [*Gradle*](http://gradle.org/).

A nossa abordagem à arquitetura do *Terasology* será baseada no modelo de 4+1 vistas de arquitetura de software. As 4 vistas representam a vista lógica, vista de implementação, vista de processo e vista de distribuição, sendo que a vista adicional (+1) se trata da vista de casos de utilização, cujo diagrama foi já apresentado no [último relatório](https://github.com/andrelago13/Terasology/blob/master/ESOF-docs/2%20-%20Requirements%20Management.md) apresentado.

Para além disso é necessário referir que nos vamos focar apenas no [*engine*](https://github.com/andrelago13/Terasology/tree/master/engine/src/main/java/org/terasology) do jogo porque esse era o propósito inicial do projeto e porque consideramos desnecessário fazer análise dos diferentes módulos existentes não só pela sua diversidade como pela sua "simplicidade" face ao *engine* em si.

<a name="logicalview"/>
## Vista Lógica

<a name="implementationview"/>
## Vista de Implementação

<a name="processview"/>
## Vista de Processo

<a name="deploymentview"/>
## Vista de Distribuição

<a name="group_contribution"/>
## Contribuição do Grupo

 - [André Machado](https://github.com/andremachado94) (up201202865@fe.up.pt): 2 horas
 - [André Lago](https://github.com/andrelago13) (up201303313@fe.up.pt): 2 horas
 - [Gustavo Silva](https://github.com/gtugablue) (up201304143@fe.up.pt): 2 horas
 - [Marina Camilo](https://github.com/Aniiram) (up201307722@fe.up.pt): 2 horas
