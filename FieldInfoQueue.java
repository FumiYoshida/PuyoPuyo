package meow2021;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;



public class FieldInfoQueue {
	// FieldInfoを保持する優先度付きキュー
	// だいたいmaou2020からのコピペ
	FastBitNextField bnf;
    HashSet<Integer> mHash = new HashSet<Integer>(10000);
    PriorityQueue<FieldInfo> mQueueImpl = new PriorityQueue<FieldInfo>(1000, sComparator);
    
	public FieldInfoQueue(FastBitNextField bnf) {
		this.bnf = bnf;
	}
	
	public void add(FieldInfo field) {
        int n = Arrays.hashCode(field.field); // 盤面しか見ていないので、衝突があり得る
        if (this.mHash.contains(n)) {
        	return;
        }
        field.evaluation = bnf.EvaluateFieldFast(field);
        this.mQueueImpl.add(field);
        this.mHash.add(n);
    }

    public boolean isEmpty() {
        return this.mQueueImpl.isEmpty();
    }

    public FieldInfo poll() {
        return this.mQueueImpl.poll();
    }
	
	static Comparator<FieldInfo> sComparator = new Comparator<FieldInfo>(){
	    @Override
	    public int compare(FieldInfo field, FieldInfo field2) {
	        return field.evaluation == field2.evaluation ? 0 : (field.evaluation < field2.evaluation ? 1 : -1);
	    }
	};
}


