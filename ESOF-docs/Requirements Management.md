# Requirements Management

<a name="index"/>
## Índice
1. [Elicitação](#elicitation)
  1. [Casos de Uso](#use_cases)
2. [Validação](#validation)
  1. [Controlo de versão](#version_control)
3. [Contribuição do Grupo](#group_contribution)

<a name="elicitation"/>
## Elicitação

  O processo de evolução do projeto no que diz respeito a requisitos é bastante simples, envolvendo não só a equipa de desenvolvimento como a comunidade envolvente.

  Os *developers* associados ao projeto trabalham para implementar funcionalidades relacionadas com os requisitos estabelecidos ou para corrigir <a name="issues"/>[*issues*](https://github.com/MovingBlocks/Terasology/issues) que estejam listadas no repositório do projeto.
 
 Para além dos requisitos pré-estabelecidos no início do desenvolvimento do projeto, muitos vão sendo adicionados à medida que são sugeridos por contribuidores ou por membros da comunidade. O processo de adição de um novo requisito é o seguinte:
 - Quando uma pessoa tem uma ideia para um novo requisito/funcionalidade para o projeto, faz uma sugestão no [fórum de sugestões](http://forum.terasology.org/forum/suggestions.21/) do **Terasology**. Se, nesta altura, perceber que essa funcionalidade já foi proposta anteriormente nesse fórum, deve perceber o estado atual dela antes de decidir como proceder.
 - Algum membro da comunidade (que pode ser o mesmo que fez a proposta de funcionalidade) faz uma estruturação preliminar da ideia no fórum, de forma a se perceber mais detalhes técnicos sobre a ideia em questão.
 - É possível que surja nesta fase inicial algum código simples necessário para a ideia se tornar viável, sendo este código partilhado no fórum.
 - Quando a ideia ganha estrutura e viabilidade, move-se para o fórum de um dos [módulos do projeto](http://forum.terasology.org/forum/modules.55/). Se a ideia servir para enriquecer o jogo a nível de apresentação, o fórum indicado será o [*Art forum*](http://forum.terasology.org/forum/art-media.25/). Se não, se for para ajudar na arquitetura na mecânica de jogo em si ou se for uma funcionalidade ao nível de bibliotecas, é encaminhado para o [*Core Projects forum*](http://forum.terasology.org/forum/core-projects.54/).
 - Após alguma discussão, a nova funcionalidade torna-se suficientemente completa para ser implementada. No *Terasology*, cada módulo vive num repositório independente (dentro da [organização *Terasology*](https://github.com/Terasology/), tendo normalmente 1 ou 2 autores. Usando a tecnologia [*Gradle*](http://gradle.org/) é possível importar diretamente o código de módulos selecionados para um determinado workspace sem haver a necessidade de manter todos os módulos do projeto juntos no mesmo local. Deste modo, cada módulo pode ser desenvolvido e testado de forma totalmente independente dos outros módulos. Assim, a nova ideia é implementada no repositório do módulo respetivo. Como normalmente cada módulo tem muito poucos membros, as diferenças são adicionadas ao *branch "master"* do módulo automáticamente (através de *merge*), mas em casos de módulos maiores usa-se o sistema usual de *pull requests*.
 - No caso de a nova funcionalidade ser referente ao "*core*" do jogo, o processo é ligeiramente diferente uma vez que, embora também este "viva" num [repositório próprio](https://github.com/MovingBlocks/Terasology), normalmente tem um grupo bastante maior de contribuidores. Assim, a metodologia a usar deve ser a de fazer *fork* do repositório respetivo, usando *pull requests* para pedir aprovação de junção das funcionalidades implementadas.
  
  Este processo permite estruturar o projeto de forma a que cada *developer* possa fazer o trabalho que considerar melhor sem atrapalhar o que outros estão a fazer. Apesar de não ser um projeto com muitos colaboradores ativos, sem este método o desenvolvimento poderia tornar-se caótico em muitas situações. Por exemplo, se um colaborador externo se lembrar de uma funcionalidade que até pode ter interesse para si mas não para os restantes elementos do projeto, pode simplesmente propôr a ideia no fórum como forma de angariar apoio e começar a desenvolvê-la no repositório do seu módulo, visto que isso não implica que todos os utilizadores utilizem esse módulo.

  Em relação às *issues* referidas [acima](#issues), é importante referir que estas podem ser reportadas por qualquer utilizador do GitHub. Isto permite criar uma espécie de *task list* de erros a corrigir, sendo também possível uma escolha de quais as *issues* a resolver primeiro tendo em conta a sua gravidade.

<a name="use_cases"/>
### Casos de Uso
Muitas vezes é importante perceber o tipo de utilização que uma determinada aplicação vai ter para determinar quais os requesitos que se podem aplicar. Assim, diagrama que se segue mostra os principais casos de uso do executável do jogo *Terasology*.

![Terasology executable use case diagram](/ESOF-docs/resources/usecasediagram-executable.png)

A nível do *engine*, sem contar com os módulos extra, as principais interações que um jogador pode efetuar com o jogo em si estão representadas no seguinte diagrama:

![Terasology game use case diagram](/ESOF-docs/resources/usecasediagram-game.png)

<a name="validation"/>
## Validação

  Uma vez que não há *deadlines* para o projeto, o desenvolvimento pode ser feito de forma cuidada, pelo que é importante o processo de validação dos requesitos para manter a estrutura sólida do código.

  Como já foi referido, o desenvolvimento do jogo é dividido em módulos que são distribuidos por pequenas equipas (1 a 3 pessoas) ou, muitas vezes, apenas pelo autor do módulo. Cada um destes módulos tem o seu próprio repositório no GitHub, permitindo aos seus autores fazer *push* ou *merge* diretamente sem a necessidade de um *pull request* ou de algum tipo de autorização.

  Ainda assim, em casos de módulos maiores em que há necessidade de *pull requests*, estes são revistos por uma ou mais pessoas familiarizadas com o módulo em questão e realizam testes para garantir que o código funciona. Normalmente, esta tarefa é executada pelo autor original do módulo. No caso de existirem erros, estes podem ser reportados nas [issues](#issues) do *Terasology*. Em algumas situações excecionais o próprio *team leader* [(*Cervator*)](https://github.com/Cervator) testa o código ou nomeia alguém mais familiar com o módulo em questão para o fazer. Mesmo havendo esta "liberdade" no desenvolvimento independente dos módulos, por vezes o *team leader* do projeto arranja algum tempo para rever as modificações feitas nos módulos mais próximos e importantes do projeto (os 80 módulos referidos no [*readme*](https://github.com/MovingBlocks/Terasology/blob/develop/README.markdown#modules) do projeto, podendo assim reportar erros aos seus autores e certificar-se da sua qualidade quando é lançada uma nova *release*.

  Por sua vez, o desenvolvimento do *engine* e de outros *frameworks* centrais tende a ser da responsabilidade de equipas grandes de contribuidores, pelo que é habitual a utilização de *pull requests* para revisão e decisão de como fazer o *merge* das alterações. Estes *pull requests* são revistos pelo [*team leader*](https://github.com/Cervator) num espaço de algumas semanas para prevenir que código incompleto ou errado vaze para alguma *release* do jogo. Assim, é da responsabilidade do *team leader* a escolha de quais modificações aceitar ou não para o jogo, bem como o lançamento de novas *releases*.

<a name="version_control"/>
### Controlo de versão

Por se tratar de um projeto *open-source* em que os contribuidores são voluntários dispostos a ajudar, torna-se díficil lançar novas *releases* a um ritmo bem definido. Apesar disso, o [*team leader*](https://github.com/Cervator) tenta fazê-lo a cada 2-4 semanas.

O nome dado a cada versão segue o padrão ["*Semantic Versioning*"](semver.org) que se resume ao seguinte:
> Dado um número de versão MAJOR.MINOR.PATCH, incremente a:

>- versão maior (MAJOR):sempre que forem feitas mudanças incompatíveis na API;
- versão menor (MINOR): quando forem adicionadas funcionalidades, mantendo compatibilidade;
- versão de correção (PATCH): quando forem corrigidas falhas, mantendo compatibilidade.

> Rótulos adicionais para pré-lançamento (*pre-release*) e meta-dados de construção (*build*) estão disponíveis como extensão ao formato MAJOR.MINOR.PATCH.

<a name="group_contribution"/>
## Contribuição do Grupo

André Machado: 3 horas

André Lago: 6 horas

Gustavo Silva: 6 horas

Marina Camilo: 5 horas

Grupo:
 - [André Machado](https://github.com/andremachado94) (up201202865@fe.up.pt)
 - [André Lago](https://github.com/andrelago13) (up201303313@fe.up.pt)
 - [Gustavo Silva](https://github.com/gtugablue) (up201304143@fe.up.pt)
 - [Marina Camilo](https://github.com/Aniiram) (up201307722@fe.up.pt)
