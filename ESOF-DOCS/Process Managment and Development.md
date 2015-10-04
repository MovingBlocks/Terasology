# Análise dos métodos de desenvolvimento e gestão do projeto Terasology

## 1. Terasology

![Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/terasology.png?raw=true)

### 1.1 O que é?

**Terasology** é um jogo de tributo ao *Minecraft* que se mantém fiel ao original a nível de aspecto e de alguns dos modos de jogo. Este projeto open-source foi desenvolvido com o intuito de estudar os procedimentos de criação de terreno 3D bem como a eficiência de técnicas de renderização em Java recorrendo à biblioteca **[LWJGL](http://www.lwjgl.org/)**. Posteriormente surgiu uma equipa responsável pela criação do jogo em si, o qual se encontra atualmente em pré-alpha.

### 1.2 História

O jogo foi criado por Benjamin Glatzel ([begla](https://github.com/begla)). Inicialmente, o projeto estava destinado a ser apenas um *voxel engine* e tinha o nome *Blockmania*. Depois da equipa crescer o objetivo era criar um nome único e inesquecível. Daí surgiu o nome *Terasology*. Este deriva da combinação de duas palavras gregas: τέρας que se traduz *teras* e λόγος que remete para *logos*. 
Desde o início o número de contribuidores foi subindo e continua a crescer o número de pessoas interessadas em colaborar. 
**Moving Blocks** é o nome da equipa e a mesma tem muitos outros projetos apesar de **Terasology** ser o mais evidenciado. 

![Benjamin "begla" Glatzel](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/begla.png?raw=true)

### 1.3 Desenvolvimento e recursos

A página do **[Terasology](http://terasology.org/)** permite fazer o download gratuito do jogo, tendo variadas apresentações do gameplay e permitindo ainda acesso a guias para quem quiser contribuir para o desenvolvimento do projeto:
1. **[Guia para o contribuidor](https://github.com/MovingBlocks/Terasology/wiki/Contributor-Guide)** - apresenta tópicos que remetem para a explicação das variadas tecnologias usadas no desenvolvimento e explica brevemente como é feita a criação de blocos e como são usadas as texturas. 
2. **[Introdução ao contribuidor](http://forum.terasology.org/forum/contributor-introductions.7/)** - remete para o fórum da **Terasology** onde os contribuidores se apresentam e onde existem mais algumas explicações para começar a contribuir.
3. **[Setup para Developers](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup)** - apresenta um tutorial de Github para criar um branch e começar o desenvolvimento.

Para além da página principal existe ainda um [fórum](http://forum.terasology.org/) onde os contribuidores comunicam entre si sobre o projeto e os principais programadores fazem avisos. Existe ainda um subreddit usado para comunicação com os jogadores principalmente.
O jogo é dado a conhecer em inúmeros outros sites e blogs como o blog do **[Moving Blocks](http://blog.movingblocks.net/blockmania/)**. 

![Screenshot do website Terasology](https://github.com/dimamo5/Terasology/blob/Filipa/ESOF-DOCS/images/site.png?raw=true)

##2. Análise do método de desenvolvimento

Dado que o Terasology conta com dezenas de contribuidores e de modo a permitir uma gestão eficaz das contribuições do projecto, os administradores optaram por usar sistemas de [issue tracking](https://github.com/MovingBlocks/Terasology/issues) e pull request do Github. Cada contribuidor participa de acordo com um método bem definido. O primeiro passo consiste em fazer fork do projecto principal. Posteriormente, sempre que o contribuidor achar conveniente poderá fazer um pull request para o repositório principal. Os pull requests serão avaliados pelos administradores do projecto que, caso aceitem, recorrerão em seguida à ferramenta *[Jenkins](https://wiki.jenkins-ci.org/display/JENKINS/Meet+Jenkins)* a fim de automatizar todo o processo de teste e verificação do pedido.
O projecto não tem um modelo bem definido devido ao número elevado de voluntários e à constante actividade/inactividade dos contribuidores, mencionada por um dos administradores. O fundador e head developer insiste em adotar o modelo BDD (Behaviour Driven-Development) - indicação dada pelo mesmo no guia de [Unit Testing](https://github.com/MovingBlocks/Terasology/wiki/Unit-Testing) para novos contribuidores. Este modelo permite maior flexibilidade e facilidade em adquirir novos colaboradores e também uma melhor comunicação entre as diferentes equipas presentes no projecto, tais como a a equipa de teste (beta-testers) e de desenvolvimento (implementação da lógica de jogo). 



