package joshua.decoder.ff;

import joshua.decoder.JoshuaConfiguration;
import joshua.decoder.chart_parser.SourcePath;
import joshua.decoder.ff.state_maintenance.DPState;
import joshua.decoder.ff.tm.Rule;
import joshua.decoder.hypergraph.HGNode;
import joshua.decoder.segment_file.Sentence;

import java.util.List;

/**
 *
 * @author Keisuke Sakaguchi <keisuke@cs.jhu.edu>
 */
public final class EditDistanceFF extends StatelessFF {
  // This computes edit distance between source and target side.;
  private int deletion_cost = 1;
  private int insersion_cost = 1;
  private int replace_cost = 1;

  // This is a constructor (You don't have to change arguments in general.)
  public EditDistanceFF(final FeatureVector weights, String[] args, JoshuaConfiguration config) {
    // referring superclass constructor, which is StatelessFF (which extends FeatureFunction)
    // (weights, name, args, config)
    super(weights, "EditDist_", args, config);

    // TODO: check if owner information is used in this class
    // TODO: understand what the owner (e.g. PhraseModel) is.
    //String owner = parsedArgs.get("owner");
    // TODO: understand what the Vocabulary class (e.g. OOVPenalty) is.
    //int ownerID = Vocabulary.id("oov");
  }

  @Override
  public DPState compute(Rule rule, List<HGNode> tailNodes, int i, int j, SourcePath sourcePath,
                         Sentence sentence, Accumulator acc) {
    String[] src_tokens = rule.getEnglishWords().split(" ");
    String[] trg_tokens = rule.getFrenchWords().split(" ");
    float exp_editdist = (float) Math.exp( (double) (editdist(src_tokens, trg_tokens)));
    acc.add(name, exp_editdist);
    return null;
  }

  @Override
  public float estimateCost(Rule rule, Sentence sentence) {
    float totalWeight = 0.0f;
    if (rule != null){
        String[] src_tokens = rule.getEnglishWords().split(" ");
        String[] trg_tokens = rule.getFrenchWords().split(" ");
        float exp_editdist = (float) Math.exp( (double) (editdist(src_tokens, trg_tokens)));
        totalWeight += weights.get(name) * exp_editdist;
        return totalWeight;
    } else {
      return totalWeight;
    }
  }
  
  // This computes exponentiated edit distance (Levenstein distance)
  // The following implementation is a more efficient version.
  // c.f. http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
  private int editdist(String[] tokens0, String[] tokens1){
    int len0 = tokens0.length + 1;
    int len1 = tokens1.length + 1;
    int[] cost = new int[len0];
    int[] newcost = new int[len0];

    for (int i=0; i<len0; i++) cost[i] = i;

    for (int j=1; j<len1; j++){
      newcost[0] = j;
      for (int i=1; i<len0; i++){
        int match = (tokens0[i-1] == tokens1[j-1]) ? 0 : replace_cost;
        int cost_rep = cost[i-1] + match;
        int cost_ins = cost[i] + insersion_cost;
        int cost_del = newcost[i-1] + deletion_cost;
        newcost[i] = Math.min(cost_rep, Math.min(cost_ins, cost_del));
      }
      int[] swap = cost; cost = newcost; newcost = swap;
    }
    return cost[len0-1];
  }
}
