#Arquitetura de Software
#### Introdução

Neste relatório iremos abordar o tema de [Software Architecture](https://msdn.microsoft.com/en-us/library/ee658098.aspx) em engenharia de *software*, a qual consiste na organização 
de um sistema de *software*, incluindo a selecção dos elementos estruturais e as interfaces na qual o sistema é composto.

#### *Software Architecture* do Terasology

A arquitectura de software no Terasology, apesar de não estabelecer uma formalização definida de início, com o desenvolvimento do 
projecto, os colaboradores sentiram necessidade de criar e definir o seu próprio [*Entity System Architecture*](https://github.com/MovingBlocks/Terasology/wiki/Entity-System-Architecture)
para uma melhor orientação e organização dos elementos estruturais.

Alguns dos principais objectivos da arquitetura do *Terasology* estão definidos no seu próprio [*forum*](http://forum.terasology.org/threads/architecture-vision.690/)
onde podemos dar realce a alguns que nos parecem relevantes destacar:

####1 
Capacidade [*modding*](https://en.wikipedia.org/wiki/Mod_(video_gaming)), com o objectivo de evitar possíveis conflitos;
introduzir novas funcionalidades; introdução de comportamentos novos no jogo; maior capacidade de resistência a potenciais erros
e alterações; segurança em particular para quem faz *downloads* de *plugins* dos servidores dedicados. 
####2 
Separação no que diz respeito à *engine* e da lógica *game/assets*. A *engine* deve-se centrar com as características e funcionalidades 
como *rendering*, som e *voxel world support*. 
A lógica do jogo é alcançada através de *modularity*, o que permite alterar qualquer conjunto de módulos com vista
a poder efectuar alterações ao modo de jogo.

O modelo pelo qual decidimos abordar a arquitectura do *Terasology* será através do [Modelo De Vistas 4 + 1](https://en.wikipedia.org/wiki/4%2B1_architectural_view_model):

* Vista Lógica - Diagrama de Pacotes
* Vista de Implementação - Diagrama de Componentes
* Vista do Processo - Diagrama de Atividades
* Vista de *Deployment* - Diagrama de *Deployment*
* Vista de Casos de Utilização (definido no 2º relatório).
##Vista Lógica
##Vista de Distribuição
##Vista de Processo
##Vista de Implementação

A maioria das partes do Sistema de Entidade Terasology são definidos como interfaces, e, em seguida, implementado apesar de um conjunto de classes começando com POJO (Plain Old Java para Object). Isso permite a possibilidade de substituir a implementação com algo mais exótico no futuro para melhorar o desempenho.

Componentes - estes fornecem dados e comportamento através da sua interacção com o sistema e são armazenados em uma entidade. Um componente é um conjunto significativo de dados, com uma intenção de serem utilizados para fornecerem um comportamento, que pode estar ligado a uma entidade. Podendo ser um objeto Java simples com um conjunto simples de objetos de valor e funcionalidade mínima.
Componentes apenas podem conter tipos de dados específicos - isto é usado para apoiar persistência.

Os componentes funcionam na prática, a partir de módulos mais simples e progredindo para os mais complexos. 

#TODO IMAGEM FELIPE

##Conclusão
##Autores e contribuição
