tree grammar PAGWalker;

options {
  language = Java;
  output = AST;
  tokenVocab = PAGDefinition;
  ASTLabelType = CommonTree;
}

@header {
  package org.terasology.asset.loaders.grammar;

  import java.util.Map;
  import java.util.HashMap;

  import java.util.ArrayList;
  import java.util.List;

  import org.terasology.logic.grammar.Successor;
  import org.terasology.logic.grammar.Symbol;
  import org.terasology.logic.grammar.SetSuccessor;
  import org.terasology.logic.grammar.SubdivSuccessor;

  import org.terasology.logic.grammar.SubdivArg;
  import org.terasology.logic.grammar.Size;
  import org.terasology.logic.grammar.SubdivSuccessor.Direction;

  import org.terasology.logic.grammar.assets.Grammar;

  import org.terasology.world.block.BlockUri;
}

@members {
  private Map<String, String> attributes = new HashMap<String, String>();
  private Map<Symbol, List<Successor>> rules = new HashMap<Symbol, List<Successor>>();
}

pag returns [Grammar result]
  : ^(PAG header rules)
      {
        // construct grammar object
      }
  ;

header
  : (assignment)+
  ;

assignment
  : ^('=' key=ID value=ID)
      { attributes.put($key.text, $value.text);}
  ;

  rules
    : (rule)+
    ;

  rule
    : ^('::-' p=predecessor s=successor)
      {
        Symbol pre = p.pred;
        if (!rules.containsKey(pre)) {
          rules.put(pre, new ArrayList<Successor>());
        }
        rules.get(pre).add(s.succ);
      }
    ;

  predecessor returns [Symbol pred]
  : ^(PRE label=ID)
      {
        // return a new Symbol with the id as label
        $pred = new Symbol($label.text);
      }
  | ^(GUARDED_PRE ID expression)
      {
        // ignore the guard for the moment
        // TODO: imlement guards
        $pred = new Symbol($label.text);
      }
  ;

 expression
    :
    ;

 successor returns [Successor succ]
    // first alternative: the successor is a set command
    : { float p = -1;}
      ^(SET block_uri
          (^(PROB probability)
            {p = $probability.prob;}
          )?
      )
      {
        if (p >= 0) {
          $succ = new SetSuccessor($block_uri.uri, p);
        }
        else {
          $succ = new SetSuccessor($block_uri.uri);
        }
      }
    // second alternative: the successor is a subdiv command
    | { float p = -1;
        List<SubdivArg> args = new ArrayList<SubdivArg>();
      }
      ^(SUBDIV d=direction
          (subdiv_arg
            { args.add($subdiv_arg.arg); }
          )+
          (^(PROB probability)
            { p = $probability.prob;}
          )?
      )
      {
        if (p >= 0) {
          $succ = new SubdivSuccessor(args, $d.direction, p);
        }
        else {
          $succ = new SubdivSuccessor(args, $d.direction);
        }
      }
    // third alternative: the successor is a component split
/*    | { float p = -1;
        List<CompArg> args = new ArrayList<CompArg>();
      }
      ^(COMP
          (comp_arg
            { args.add($subdiv_arg.arg); }
          )+
          (^(PROB probability)
            { p = $probability.prob;}
          )?
      )
      {
        if (p >= 0) {
          $succ = new SubdivSuccessor(args, $d.direction, p);
        }
        else {
          $succ = new SubdivSuccessor(args, $d.direction);
        }
      } */
    ;

subdiv_arg returns [SubdivArg arg]
  : ^(SUBDIV_ARG size successor)
      { $arg = new SubdivArg($size.size, $successor.succ); }
  ;

size returns [Size size]
  : {boolean absolute = true;}
    ^(SIZE INTEGER
        (RELATIVE
          {absolute = false;}
        )?
     )
     {
      if (!absolute){
        $size = new Size(Integer.parseInt($INTEGER.text)/100, false);
      } else {
        $size = new Size(Integer.parseInt($INTEGER.text), true);
      }
     }
  ;


probability returns [float prob]
  :
  ;

direction returns [Direction direction]
  : X { $direction = Direction.X;}
  | Y { $direction = Direction.Y;}
  | Z { $direction = Direction.Z;}
  ;

/* Contsructs a BlockUri from the given String.
 * For parsing the constructor of BlockUri is used.
 */
block_uri returns [BlockUri uri]
      : {StringBuilder b  = new StringBuilder();}
        ^(BLOCK_URI packageName=ID familyName=ID
            { b.append($packageName.text + ':' + $familyName.text); }
            (shapePackageName=ID shapeName=ID
              { b.append(':' + $shapePackageName.text + ':' + shapeName); }
            )?
            (blockIdentifier=ID
              { b.append( '.' + $blockIdentifier.text); }
            )?
          )
          { $uri = new BlockUri(b.toString()); }
      ;


