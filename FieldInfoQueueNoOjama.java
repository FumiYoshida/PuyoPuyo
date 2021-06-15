package meow2021;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class FieldInfoQueueNoOjama {
	// long[]型でおじゃまがない場合の盤面を保持する優先度付きキュー
	// 配列の最後はdouble型の盤面の評価値をビットをそのままにlong型に変換したものを入れる
	// だいたいmaou2020からのコピペ
	
	FastBitNextFieldNoOjama bnf;
    HashSet<Integer> mHash = new HashSet<Integer>(10000);
    PriorityQueue<long[]> mQueueImpl = new PriorityQueue<long[]>(1000, sComparator);
    
	public FieldInfoQueueNoOjama(FastBitNextFieldNoOjama bnf) {
		this.bnf = bnf;
	}
	
	public void add(long[] field) {
		long[] board = new long[6];
		for (int i=0;i<6;i++) {
			board[i] = field[i];
		}
        int n = Arrays.hashCode(board); // 盤面しか見ていないので、衝突があり得る
        if (this.mHash.contains(n)) {
        	return;
        }
        if (Double.longBitsToDouble(field[6]) == 0) {
            field[6] = Double.doubleToLongBits(bnf.EvaluateFieldFast(board));
        }
        this.mQueueImpl.add(field);
        this.mHash.add(n);
    }

    public boolean isEmpty() {
        return this.mQueueImpl.isEmpty();
    }

    public long[] poll() {
        return this.mQueueImpl.poll();
    }
	
	static Comparator<long[]> sComparator = new Comparator<long[]>(){
	    @Override
	    public int compare(long[] field, long[] field2) {
	        return field[6] == field2[6] ? 0 : (Double.longBitsToDouble(field[6]) < Double.longBitsToDouble(field2[6]) ? 1 : -1);
	    }
	};
}
