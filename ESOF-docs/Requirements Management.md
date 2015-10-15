# Requirements Management

## Elicitation

  O processo de evolução do projecto não é nada de muito formal. No entanto, existem algumas regras para que ninguém se atrapalhe. Os developers contêm uma lista de problemas para identificar os erros e funcionalidades que precisam de ser corrigidos (como referido no relatório anterior), um forum de sugestões para registar as ideias (http://forum.terasology.org/forum/suggestions.21/), e outros foruns para levar as sugestões à sua implementação. Normalmente isto acontece quando um novo contribuidor aparece e quer fazer alguma coisa. O ideal seria ele seguir estes passos:
  
 - Alguém faz uma sugestão no forum. Se for uma boa ideia passa para o ponto seguinte.
 - Alguém pega na ideia e começa a estruturá-la, escrevendo mais notas técnicas e detalhes da sua implementação.
 - Alguém começa a escrever o seu código. Normalmente costuma ser a mesma pessoa. Este é o ponto onde o processo realmente começa.
 - Move-se o conceito para um fórum de um modulo se este se relacionar com algum (é o que acontece normalmente). Se for para enriquecer o jogo a nível de apresentação, o fórum indicado será o art forum. Se não, se for para ajudar na arquitetura na mecânima de jogo em si ou se for uma funcionalidade ao nível de bibliotecas, é encaminhado para o Core Projects fórum.
 - A nova funcionalidade acaba por ficar suficientemente completa para se colocar em uso. Todos os modulos vivem nos seus próprios repositorios no GitHub, assim os autores podem fazer push directamente sem fazer um pull requests (PRs). Quando chega ao nível dos pull requests (PRs), estas funcionalidades passam por uma revisão e são incluidas no projecto (merged). 
 - Lançamos a nova funcionalidade no lançamento da versão seguinte do Jogo.
  
  Quanto ao porquê deste processo. É uma forma de colocar alguma estrutura sem atrapalhar o trabalho das pessoas que querem trabalhar. Nota-se que muitas vezes arranjam-se alguns atalhos quando alguém está estusiasmado com uma nova funcionalidade e aparece com a mesma do nada. Normalmente a primeira vez que a comunidade aprende sobre uma nova funcionalidade, ou conteudo é quando um autor anuncia ou envia um pull request. É um pouco dificil arranjar alguma estrutura no meio deste ambiente. O que torna o planeamento algo bastante dificil. 
  A equipa/equipas são geridas inteiramente por individuos em que todos contribuem ao seu ritmo e dependendo da sua disponibilidade. Esta é uma grande desvantagem em contrapartida a trabalhar neste projecto a tempo inteiro. O team leader deste projecto é o Cervator. No entanto, ele apenas encoraja e delega o trabalho pelas pessoas. A sua posição é normalmente fazer algo que seja essencial mas que nimguém queira fazer, o que se torna difícil devido à pouca disponibilidade que tem.
  Não existem deadlines, permitindo orientar o trabalho para o que é mesmo necessário na altura que, com um pouco de sorte, é feito em poucos meses.
  Quanto aos erros, toda a gente pode reportá-los no GitHub. Ocasionalmente eles tentam encarregar uma pessoa para analisá-los e ás vezes corrigi-los. O que costuma acontecer é precisamente o contrário. Os erros ficam lá até alguém se lembrar e querer corrigi-los. 

## Validation

  A equipa consegue normalmente lançar uma nova versão a cada 2-4 semanas, mas não é constante uma vez que depende do que cada pessoa faz. Será que existe conteudo suficiente para sair uma versão do jogo? Quando sai uma nova versão do jogo, esta contem uma grande lista de modulos considerados estáveis e inclui a base do jogo, mas é melhorado por diferentes pessoas o que torna dificil realizar updates nesse nível.
  Os pull requests são revistos pelo menos por uma pessoa que seja familiar com a funcionalidade sem ser o autor. Essa pessoa pega no código e realiza testes para se certificar que funciona. Depois, ou reporta erros ao autor ou realiza o merge para um nivel superior. Normalmente os pull requests são feitos onde o autor é a única pessoa associada, o que torna o raciocinio anterior dificil de implementar. Nestes casos, o próprio team leader(Cervator) testa o código ou arranja alguem que seja mais familiar com aquele modulo para o fazer.  
