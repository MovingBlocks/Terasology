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

  O processo de evolução do projeto no que diz respeito a requesitos é bastante simples, envolvendo não só a equipa de desenvolvimento como a comunidade envolvente.

  Os *developers* associados ao projeto trabalham para implementar funcionalidades relacionadas com os requesitos estabelecidos ou para corrigir [*issues*](https://github.com/MovingBlocks/Terasology/issues) que estejam listadas no repositório do projeto.
 
 Para além dos requisitos pré-estabelecidos no início do desenvolvimento do projeto (provavelmente, a maior parte destes foi já cumprido), muitos vão sendo adicionados à medida que são sugeridos por contribuidores ou por membros da comunidade. O processo de adição de um novo requesito é o seguinte:
 - Quando uma pessoa tem uma ideia para um novo requesito/funcionalidade para o projeto, faz uma sugestão no [fórum de sugestões](http://forum.terasology.org/forum/suggestions.21/) do **Terasology**. Se, nesta altura, perceber que essa funcionalidade já foi proposta anteriormente nesse fórum, deve perceber o estado atual dela antes de decidir como proceder.
 - Algum membro da comunidade (que pode ser o mesmo que fez a proposta de funcionalidade) faz uma estruturação preliminar da ideia no fórum, de forma a se perceber mais detalhes técnicos sobre a ideia em questão.
 - É possível que surja nesta fase inicial algum código elementar necessário para a ideia se tornar viável, sendo este código partilhado no fórum.
 - Quando a ideia ganha estrutura e viabilidade, move-se para o fórum de um dos [módulos do projeto](http://forum.terasology.org/forum/modules.55/). Se for para enriquecer o jogo a nível de apresentação, o fórum indicado será o [*Art forum*](http://forum.terasology.org/forum/art-media.25/). Se não, se for para ajudar na arquitetura na mecânica de jogo em si ou se for uma funcionalidade ao nível de bibliotecas, é encaminhado para o [*Core Projects forum*](http://forum.terasology.org/forum/core-projects.54/).
 - Após alguma discussão, a nova funcionalidade torna-se suficientemente completa para ser implementada. No *Terasology*, cada módulo vive num repositório independente (dentro da [organização *Terasology*](https://github.com/Terasology/), tendo normalmente 1 ou 2 autores. Usando a tecnologia [*Gradle*](http://gradle.org/) é possível importar diretamente o código de módulos selecionados para um determinado workspace sem haver a necessidade de manter todos os módulos do projeto juntos no mesmo local. Deste modo, cada módulo pode ser desenvolvido e testado de forma totalmente independente dos outros módulos. Assim, a nova ideia é implementada no repositório do módulo respetivo. Como normalmente cada módulo tem muito poucos membros, as diferenças são adicionadas ao *branch "master"* automáticamente, mas em casos de módulos maiores usa-se o sistema usual de *pull requests*.
 - No caso de a nova funcionalidade ser referente ao "*core*" do jogo, o processo é ligeiramente diferente porque, embora também este "viva" num [repositório próprio](https://github.com/MovingBlocks/Terasology), normalmente tem um grupo bastante maior de contribuidores. Assim, a metodologia a usar deve ser a de fazer *fork* do repositório respetivo, usando *pull requests* para pedir aprovação de junção das funcionalidades implementadas. A cada 2 a 4 semanas um dos responsáveis pelo projeto (normalmente o ["*Cervator*"](https://github.com/Cervator)) dedica algum tempo a rever as *pull requests* efetuadas, decidindo quais adicionar à *release* seguinte do jogo.
  
  Este processo é uma forma de colocar alguma estrutura sem atrapalhar o trabalho das pessoas que querem trabalhar. Nota-se que muitas vezes arranjam-se alguns atalhos quando alguém está estusiasmado com uma nova funcionalidade e aparece com a mesma do nada. Normalmente a primeira vez que a comunidade aprende sobre uma nova funcionalidade, ou conteudo é quando um autor anuncia ou envia um pull request. É um pouco dificil arranjar alguma estrutura no meio deste ambiente. O que torna o planeamento algo bastante dificil. 
  A equipa/equipas são geridas inteiramente por individuos em que todos contribuem ao seu ritmo e dependendo da sua disponibilidade. Esta é uma grande desvantagem em contrapartida a trabalhar neste projecto a tempo inteiro. O team leader deste projecto é o Cervator. No entanto, ele apenas encoraja e delega o trabalho pelas pessoas. A sua posição é normalmente fazer algo que seja essencial mas que nimguém queira fazer, o que se torna difícil devido à pouca disponibilidade que tem.
  Contudo, esta equipa consegue normalmente lançar uma nova versão a cada 2-4 semanas, mas não é constante uma vez que depende do que cada pessoa faz. Será que existe conteudo suficiente para sair uma versão do jogo? Quando sai uma nova versão do jogo, esta contem uma grande lista de modulos considerados estáveis e inclui a base do jogo, mas é melhorado por diferentes pessoas o que torna dificil realizar updates nesse nível.
  Não existem deadlines, permitindo orientar o trabalho para o que é mesmo necessário na altura que, com um pouco de sorte, é feito em poucos meses.
  Quanto aos erros, toda a gente pode reportá-los no GitHub. Ocasionalmente eles tentam encarregar uma pessoa para analisá-los e ás vezes corrigi-los. O que costuma acontecer é precisamente o contrário. Os erros ficam lá até alguém se lembrar e querer corrigi-los. 
  Os pull requests são revistos pelo menos por uma pessoa que seja familiar com a funcionalidade sem ser o autor. Essa pessoa pega no código e realiza testes para se certificar que funciona. Depois, ou reporta erros ao autor ou realiza o merge para um nivel superior. Normalment os pull requests são feitos onde o autor é a única pessoa associada, o que torna o raciocinio anterior dificil de implementar. Nestes casos, o próprio team leader testa o código ou arranja alguem que seja mais familiar com aquele modulo para o fazer.  

<a name="use_cases"/>
### Casos de Uso
O diagrama que se segue mostra os principais casos de uso do jogo Terasology.

![Terasology Use Cases](/ESOF-docs/resources/usecasediagram.png)

<a name="validation"/>
## Validação

O desenvolvimento do jogo é dividido em módulos que são distribuidos por pequena equipas, muitas vezes apenas pelo autor do módulo. Cada um destes módulos tem o seu próprio repositório no GitHub, permitindo aos seus autores fazer push diretamente sem a necessidade de um *pull request*.

Os *pull requests* associados a esses módulos são revistos por uma ou mais pessoas familiares com a funcionalidade que analisam a atualização em questão e realizam testes para garantir que o código funciona. Posteriormente, ou é feito *merge* do novo código ou os erros são reportados ao autor do *pull request*. Na maior parte dos casos os *pull requests* são feitos pelo autor, que é a unica pessoa associada/ a trabalhar nesse módulo. Nestes casos, o próprio *team leader* [(*Cervator*)](https://github.com/Cervator) testa o código ou nomeia alguém mais familiar com o módulo em questão para o fazer.

No entanto, o desenvolvimento do *engine* e de outros *frameworks* centrais tende a ser da responsabilidade de vários contribuidores, pelo que é habitual a utilização de *pull requests* para revisão e decisão de como fazer o *merge* das alterações.

<a name="version_control"/>
### Controlo de versão

Por se tratar de um projeto *open-source* em que os contribuidores são voluntários dispostos a ajudar, torna-se díficil lançar novas *releases* a um ritmo bem definido. Apesar disso, o *team leader* tenta fazê-lo a cada 2-4 semanas.

O nome dado a cada versão segue o padrão ["*Semantic Versioning*"](semver.org) que se resume ao seguinte:
> Dado um número de versão MAJOR.MINOR.PATCH, incremente a:

>- versão maior (MAJOR):sempre que forem feitas mudanças incompatíveis na API;
- versão menor (MINOR): quando forem adicionadas funcionalidades, mantendo compatibilidade;
- versão de correção (PATCH): quando forem corrigidas falhas, mantendo compatibilidade.

> Rótulos adicionais para pré-lançamento (*pre-release*) e meta-dados de construção (*build*) estão disponíveis como extensão ao formato MAJOR.MINOR.PATCH.

<a name="group_contribution"/>
## Contribuição do Grupo

André Machado: 2 horas
André Lago: 8 horas
Gustavo Silva: 8 horas
Marina Camilo: 2 horas

Grupo:
 - [André Machado](https://github.com/andremachado94) (up201202865@fe.up.pt)
 - [André Lago](https://github.com/andrelago13) (up201303313@fe.up.pt)
 - [Gustavo Silva](https://github.com/gtugablue) (up201304143@fe.up.pt)
 - [Marina Camilo](https://github.com/Aniiram) (up201307722@fe.up.pt)
