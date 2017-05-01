# Relatório 1

## Descrição do Projeto

[**Terasology**](http://terasology.org/) é um jogo em Java com um mundo aberto em 3D que tenta adicionar alguma sofisticação ao jogo da Mojang, "Minecraft", no qual foi inspirado.

![Terasology Gameplay 1](/ESOF-docs/resources/gameplay1.jpg)

O objetivo do Terasology é de servir como ferramenta de investigação de geração procedimental de terreno (["*procedural terrain generation*"](https://en.wikipedia.org/wiki/Procedural_generation) - uma técnica de gerar terreno de forma algorítmica que minimiza o espaço em disco do jogo) e técnicas de [*rendering*](https://en.wikipedia.org/wiki/Rendering_(computer_graphics)) em Java.

O jogo consiste num mundo amplo feito de blocos cúbicos que podem representar todo o tipo de materials como terra, relva, pedra, madeira ou ferro. O utilizador controla uma personagem que se move pelo mundo, tentando sobreviver a criaturas selvagens e procurar materiais raros.

A partir do repositório conseguimos perceber que este foi criado em Fevereiro de 2011, pelo que o projeto em si deve ter surgido poucos meses antes. Ao todo existem mais de 60 contribuidores no projeto, embora apenas 16 tenham estado ativos durante este ano (2015). O criador do projeto foi Benjamin Glatzel (["*begla*"](https://github.com/begla)) que, juntamente com 3 outros utilizadores (["*immortius*"](https://github.com/immortius), ["*Cervator*"](https://github.com/Cervator) e ["*msteiger*"](https://github.com/msteiger)), constitui o núcleo principal de desenvolvimento do projeto.

O Terasology também possui uma comunidade grande em várias redes sociais ([Facebook](https://www.facebook.com/Terasology), [Twitter](https://twitter.com/terasology), [Google+](https://plus.google.com/103835217961917018533/posts), ...).
As *pre-alpha releases* são divulgadas publicamente por esses meios, pelo que podem ser [descarregadas por utilizadores que não sejam programadores de forma fácil](https://github.com/MovingBlocks/Terasology/releases).

O projeto está licenciado sob a [licença Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

![Terasology Gameplay 2](/ESOF-docs/resources/gameplay2.png)

## Processo de desenvolvimento

### Desenvolvimento do projeto

Os contribuidores do projeto dividem-se entre equipas dedicadas a certas partes do projeto, sendo possível que o mesmo contribuidor trabalhe para mais do que uma equipa. As diferentes equipas são:
 - Arquitetura
 - Arte
 - Design
 - Geral
 - GUI
 - Logística
 - Mundo

O sistema de *issues* do GitHub é amplamente utilizado para controlar tarefas, melhorias e bugs. Algumas dessas *issues* estão incluidas em *milestones* do projeto, que definem objetivos a atingir como, por exemplo, um estado considerável *alpha* e um estado considerável *beta* do desenvolvimento jogo.

### Contribuições

Para contribuir é necessário criar um *fork* do repositório original, escrevendo-se o código no novo repositório criado. Quando se considerar que foi implementada ou corrigida uma funcionalidade que deve ser adicionada ao projeto original inicia-se uma *pull request* para que o dono do repositório de origem decida se pretende juntar as alterações ao seu projeto.

### Processo de software

O projeto utiliza o modelo **Behavior Driven Development** (BDD) por vários motivos. Por um lado, pretende-se estimular a participação de contribuidores externos para promover a comunicação entre eles e a prática de Desenvolvimento de Fora para Dentro (*Outside-In Development*). O objetivo é que os diversos contribuidores desenvolvam diferentes módulos da aplicação final com base em padrões pré-estabelecidos de comunicação entre módulos. Por exemplo, se o fundador do projeto estipula que o input dos controlos do utilizador deve ser feito de uma certa forma, qualquer contribuidor externo deve desenvolver o seu módulo tendo em conta esta informação.

Para além disso, consideramos que o modelo **Software Prototyping** (IDD) é também utilizado uma vez são frequentemente divulgadas [*pre-alpha releases*](https://github.com/MovingBlocks/Terasology/releases) que permitem a qualquer utilizador testar o jogo e experimentar o que foi desenvolvido.

### Estrutura do repositório

O repositório tem diversos *branches*, mas apenas alguns estão ainda ativos (["*develop*"](https://github.com/andrelago13/Terasology/tree/develop) e ["*weblate*"](https://github.com/andrelago13/Terasology/tree/weblate)). Pelo que podemos observar os *developers* envolvidos adicionam o seu código ao *branch* "*develop*". Mais tarde, os gerentes do repositório fundem esse *branch* com o *branch* "*master*" aquando do lançamento de uma nova *release* ou protótipo estável.

Como foi dito, as [*releases*](https://github.com/MovingBlocks/Terasology/releases) no repositório representam lançamentos *pre-alpha* do jogo, permitindo que utilizadores instalem e joguem mesmo que não sejam programadores. A descrição das *releases* detalha as funcionalidades que foram adicionadas ou adicionadas.

## Análise Crítica

### Atividade

O projeto encontra-se ativo, com uma média diária de *commits* superior a 2.

Existem cerca de 300 *issues* abertas, algumas das quais possuem a tag *Contributor-friendly*, o que significa que são de resolução relativamente acessível por qualquer contribuidor com alguns conhecimentos técnicos das ferramentas utilizadas. Isto é, na opinião do grupo, uma ótima forma de incentivar às contribuições por parte de terceiros, independentemente das suas capacidades.

Além disso, a 1 de Outubro foi criado um [*post*](http://forum.terasology.org/threads/contribute-to-open-source-get-a-free-shirt.1384/#post-12439) no fórum do Terasology a divulgar uma campanha de incentivo às contribuições em projetos open-source, organizada pelo GitHub e pela DigitalOcean.

### Estrutura do Repositório

Tendo em conta o projeto em análise, a estrutura de branches do repositório é, para nós, adequada, visto permitir o desenvolvimento paralelo de diferentes partes do projeto de uma forma organizada e independente.

### Desenvolvimento

Um dos problemas do processo de software usado neste projeto é o facto de não haver um plano bem delineado em relação ao futuro, apesar da utilização do sistema de *Milestones*. Uma prova disso pode ser obtida comparando o objetivo inicial do projeto com aquilo em que se tornou atualmente. No ínicio, o Terasology foi criado como ferramenta de investigação de geração procedimental de terreno e de técninas de *rendering* em Java. Neste momento, uma grande parte do desenvolvimento é focado em torná-lo num jogo atrativo para o jogador final.

O projeto tem vindo a alterar o seu processo de software para um do tipo *Behavior-driven Development* (BDD). Nesse sentido, a ferramenta *Jenkins* é utilizada para automatizar os testes aos *pull requests*, correndo os testes *JUnit* e verificando a existência de erros e *warnings*. Na nossa opinião a adoção deste modelo é benéfica para o projeto porque não só ajuda a estruturar a forma como ele é desenvolvido, organizando os objetivos de cada contribuidor, mas também apela à participação de programadores externos permitindo não só uma maior variedade de ideias como um alargamento da comunidade envolvente.

## Links externos

Página oficial do projeto: http://terasology.org/

Grupo:
 - [André Machado](https://github.com/andremachado94) (up201202865@fe.up.pt)
 - [André Lago](https://github.com/andrelago13) (up201303313@fe.up.pt)
 - [Gustavo Silva](https://github.com/gtugablue) (up201304143@fe.up.pt)
 - [Marina Camilo](https://github.com/Aniiram) (up201307722@fe.up.pt)
