# Análise dos métodos de desenvolvimento e gestão do projeto Terasology

## 1. Terasology

![Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/terasology.png?raw=true)

**Terasology** é um jogo de tributo ao *Minecraft* que se mantém fiel ao original a nível de aspecto e de alguns dos modos de jogo. Este projecto open-source inicialmente chamado de “Blockmania” foi desenvolvido com o intuito de estudar os procedimentos de criação de terreno 3D bem como a eficiência de técnicas de renderização em Java recorrendo à biblioteca **[LWJGL](http://www.lwjgl.org/)**. Posteriormente surgiu uma equipa responsável pela criação do jogo em si, mudando o nome do mesmo para “Terasology”, o qual se encontra atualmente em pré-alpha.

##2. Análise do método de desenvolvimento

Dado que o Terasology conta com dezenas de contribuidores e de modo a permitir uma gestão eficaz das contribuições do projecto, os administradores optaram por usar sistemas de [issue tracking](https://github.com/MovingBlocks/Terasology/issues) e pull request do Github. Cada contribuidor participa de acordo com um método bem definido. O primeiro passo consiste em fazer fork do projecto principal. Posteriormente, sempre que o contribuidor achar conveniente poderá fazer um pull request para o repositório principal. Os pull requests serão avaliados pelos administradores do projecto que, caso aceitem, recorrerão em seguida à ferramenta *[Jenkins](https://wiki.jenkins-ci.org/display/JENKINS/Meet+Jenkins)* a fim de automatizar todo o processo de teste e verificação do pedido.
O projecto não tem um modelo bem definido devido ao número elevado de voluntários e à constante actividade/inactividade dos contribuidores, mencionada por um dos administradores. O fundador e head developer insiste em adotar o modelo BDD (Behaviour Driven-Development) - indicação dada pelo mesmo no guia de [Unit Testing](https://github.com/MovingBlocks/Terasology/wiki/Unit-Testing) para novos contribuidores. Este modelo permite maior flexibilidade e facilidade em adquirir novos colaboradores e também uma melhor comunicação entre as diferentes equipas presentes no projecto, tais como a a equipa de teste (beta-testers) e de desenvolvimento (implementação da lógica de jogo). 

![Screenshot do website Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/site.png?raw=true)
