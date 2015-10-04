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

## Processo

### Processo de software

O Terasology usa um modelo de prototipagem de software.
Os contribuidores estão divididos em equipas: arquitetura, arte, design, geral, GUI, logística, mundo.

Através da análise do repositório é percetível que os contribuidores se dividem em equipas dedicadas a certas partes do projeto, sendo possível que o mesmo contribuidor trabalhe para mais do que uma equipa. As diferentes equipas são:
 - Arquitetura
 - Arte
 - Design
 - Geral
 - GUI
 - Logística
 - Mundo
 
Para além disso, o projeto utiliza o modelo de prototipagem de software uma vez que são feitas várias *pre-alpha releases* que podem ser consideradas como protótipos uma vez que apresentam estabilidade crescente e refletem as funcionalidades entretanto adicionadas/melhoradas.

O sistema de *issues* do GitHub é amplamente utilizado para controlar tarefas, melhorias e bugs. Algumas dessas *issues* estão incluidas em *milestones* do projeto, que definem objetivos a atingir como, por exemplo, um estado considerável *alpha* e um estado considerável *beta* do desenvolvimento jogo.

### Estrutura do repositório

O repositório tem diversos *branches*, mas apenas alguns estão ainda ativos (["*develop*"](https://github.com/andrelago13/Terasology/tree/develop) e ["*weblate*"](https://github.com/andrelago13/Terasology/tree/weblate)). Pelo que podemos observar os *developers* envolvidos adicionam o seu código ao *branch* "*develop*". Mais tarde, os gerentes do repositório fundem esse *branch* com o *branch* "*master*" aquando do lançamento de uma nova *release* ou protótipo estável.

Como foi dito, as [*releases*](https://github.com/MovingBlocks/Terasology/releases) no repositório representam lançamentos *pre-alpha* do jogo, permitindo que utilizadores instalem e joguem mesmo que não sejam programadores. A descrição das *releases* detalha as funcionalidades que foram adicionadas ou adicionadas.

## Análise Crítica

### Atividade
O projeto encontra-se ativo, com uma média diária de *commits* superior a 2.

Existem cerca de 300 *issues* abertas, algumas das quais possuem a tag *Contributor-friendly*, o que significa que são de resolução relativamente acessível por qualquer contribuidor com alguns conhecimentos técnicos das ferramentas utilizadas. Isto é, na opinião do grupo, uma ótima forma de incentivar às contribuições por parte de terceiros, independentemente das suas capacidades.

Além disso, a 1 de Outubro foi criado um [*post*](http://forum.terasology.org/threads/contribute-to-open-source-get-a-free-shirt.1384/#post-12439) no fórum do Terasology a divulgar uma campanha de incentivo às contribuições em projetos open-source, organizada pelo GitHub e pela DigitalOcean.

### Desenvolvimento e Milestones

## Links externos

Página oficial do projeto: http://terasology.org/

Grupo:
 - [André Machado](https://github.com/andremachado94) (up201202865@fe.up.pt)
 - [André Lago](https://github.com/andrelago13) (up201303313@fe.up.pt)
 - [Gustavo Silva](https://github.com/gtugablue) (up201304143@fe.up.pt)
 - [Marina Camilo](https://github.com/Aniiram) (up201307722@fe.up.pt)
