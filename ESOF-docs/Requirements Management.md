# Requirements Management

## Elicitação

  O processo de evolução do projeto no que diz respeito a requesitos é bastante simples, envolvendo não só a equipa de desenvolvimento como a comunidade envolvente.

  Os *developers* associados ao projeto trabalham para implementar funcionalidades relacionadas com os requesitos estabelecidos ou para corrigir [*issues*](https://github.com/MovingBlocks/Terasology/issues) que estejam listadas no repositório do projeto.
 
 Para além dos requisitos pré-estabelecidos no início do desenvolvimento do projeto (provavelmente, a maior parte destes foi já cumprido), muitos vão sendo adicionados à medida que são sugeridos por contribuidores ou por membros da comunidade. O processo de adição de um novo requesito é o seguinte:
 - Quando uma pessoa tem uma ideia para um novo requesito/funcionalidade para o projeto, faz uma sugestão no [fórum de sugestões](http://forum.terasology.org/forum/suggestions.21/) do **Terasology**. Se, nesta altura, perceber que essa funcionalidade já foi proposta anteriormente nesse fórum, deve perceber o estado atual dela antes de decidir como proceder.
 - Algum membro da comunidade (que pode ser o mesmo que fez a proposta de funcionalidade) faz uma estruturação preliminar da ideia no fórum, de forma a se perceber mais detalhes técnicos sobre a ideia em questão.
 - É possível que surja nesta fase inicial algum código elementar necessário para a ideia se tornar viável, sendo este código partilhado no fórum.
 - Quando a ideia ganha estrutura e viabilidade, move-se para o fórum de um dos [módulos do projeto](http://forum.terasology.org/forum/modules.55/). Se for para enriquecer o jogo a nível de apresentação, o fórum indicado será o [*art forum*](http://forum.terasology.org/forum/art-media.25/). Se não, se for para ajudar na arquitetura na mecânica de jogo em si ou se for uma funcionalidade ao nível de bibliotecas, é encaminhado para o [*Core Projects fórum*](http://forum.terasology.org/forum/core-projects.54/).

 ====CONTINUAR
 - A nova funcionalidade acaba por ficar suficientemente completa para se colocar em uso. Todos os modulos vivem nos seus próprios repositorios no GitHub, assim os autores podem fazer push directamente sem fazer um pull requests (PRs). Quando chega ao nível dos pull requests (PRs), estas funcionalidades passam por uma revisão e são incluidas no projecto (merged). 
 - Lançamos a nova funcionalidade no lançamento da versão seguinte do Jogo.
  
  Quanto ao porquê deste processo. É uma forma de colocar alguma estrutura sem atrapalhar o trabalho das pessoas que querem trabalhar. Nota-se que muitas vezes arranjam-se alguns atalhos quando alguém está estusiasmado com uma nova funcionalidade e aparece com a mesma do nada. Normalmente a primeira vez que a comunidade aprende sobre uma nova funcionalidade, ou conteudo é quando um autor anuncia ou envia um pull request. É um pouco dificil arranjar alguma estrutura no meio deste ambiente. O que torna o planeamento algo bastante dificil. 
  A equipa/equipas são geridas inteiramente por individuos em que todos contribuem ao seu ritmo e dependendo da sua disponibilidade. Esta é uma grande desvantagem em contrapartida a trabalhar neste projecto a tempo inteiro. O team leader deste projecto é o Cervator. No entanto, ele apenas encoraja e delega o trabalho pelas pessoas. A sua posição é normalmente fazer algo que seja essencial mas que nimguém queira fazer, o que se torna difícil devido à pouca disponibilidade que tem.
  Contudo, esta equipa consegue normalmente lançar uma nova versão a cada 2-4 semanas, mas não é constante uma vez que depende do que cada pessoa faz. Será que existe conteudo suficiente para sair uma versão do jogo? Quando sai uma nova versão do jogo, esta contem uma grande lista de modulos considerados estáveis e inclui a base do jogo, mas é melhorado por diferentes pessoas o que torna dificil realizar updates nesse nível.
  Não existem deadlines, permitindo orientar o trabalho para o que é mesmo necessário na altura que, com um pouco de sorte, é feito em poucos meses.
  Quanto aos erros, toda a gente pode reportá-los no GitHub. Ocasionalmente eles tentam encarregar uma pessoa para analisá-los e ás vezes corrigi-los. O que costuma acontecer é precisamente o contrário. Os erros ficam lá até alguém se lembrar e querer corrigi-los. 
  Os pull requests são revistos pelo menos por uma pessoa que seja familiar com a funcionalidade sem ser o autor. Essa pessoa pega no código e realiza testes para se certificar que funciona. Depois, ou reporta erros ao autor ou realiza o merge para um nivel superior. Normalment os pull requests são feitos onde o autor é a única pessoa associada, o que torna o raciocinio anterior dificil de implementar. Nestes casos, o próprio team leader testa o código ou arranja alguem que seja mais familiar com aquele modulo para o fazer.  

## Validação

O desenvolvimento do jogo é amplamente baseado em módulos que são constituidos por equipas reduzidas, muitas vezes apenas pelo autor do módulo. Cada um destes módulos vive no seu próprio repositório no GitHub, por isso os seus autores podem fazer push diretamente sem a necessidade de um *pull request*.

No entanto, o desenvolvimento do *engine* e de outros *frameworks* centrais tende a ser da responsabilidade de vários contribuidores, pelo que é habitual a utilização de *pull requests* para revisão e decisão de como fazer o *merge* das alterações.

  A equipa consegue normalmente lançar uma nova versão a cada 2-4 semanas, mas não é constante uma vez que depende do que cada pessoa faz. Será que existe conteudo suficiente para sair uma versão do jogo? Quando sai uma nova versão do jogo, esta contem uma grande lista de modulos considerados estáveis e inclui a base do jogo, mas é melhorado por diferentes pessoas o que torna dificil realizar updates nesse nível.
  Os pull requests são revistos pelo menos por uma pessoa que seja familiar com a funcionalidade sem ser o autor. Essa pessoa pega no código e realiza testes para se certificar que funciona. Depois, ou reporta erros ao autor ou realiza o merge para um nivel superior. Normalmente os pull requests são feitos onde o autor é a única pessoa associada, o que torna o raciocinio anterior dificil de implementar. Nestes casos, o próprio team leader(Cervator) testa o código ou arranja alguem que seja mais familiar com aquele modulo para o fazer. 

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
