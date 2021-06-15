package meow2021;

import java.util.Arrays;
import java.util.List;

public class FieldInfo {
	// ビット演算で使うフィールドの情報を保持しておくクラス
	public long[] field;
	public long[] ojama;
	public long[][] fieldafteravailableactions;
	public int[][] availableactions; 
	// availableactions[i][0] には(i+1)番目の可能な行動におけるぷよをrotateする回数が、
	// availableactions[i][1] には(i+1)番目の可能な行動におけるぷよを置く位置(columnnum)が入る。
	
	public int score;
	public int nrensa;
	public double firepossibility;
	public int numtofire;
	public List<int[]> scorepotentials; 
	
	public int[] piecetofire; // それぞれの色について、いくつあれば発火できるか
	public boolean[][] topinfo; // それぞれの色について、一番少ない数で発火するためにはどの列に置けばよいか
	
	public boolean[][] placetofire1;
	public boolean[][] placetofire2;
	
	public int firstpuyo;
	public int secondpuyo;
	public int nextfirstpuyo;
	public int nextsecondpuyo;
	public int nextnextfirstpuyo;
	public int nextnextsecondpuyo;
	
	public int[] ojamaList;
	public int scoreCarry; // おじゃまを相殺した後の点数
	public double evaluation; // 盤面の評価値
	public int firstActionIndex;
	
	public FieldInfo() {
		// AvailableFields 、ReadField で呼ばれるコンストラクタ
		field = new long[6];
		ojama = new long[6];
		score = 0;
		nrensa = 0;
	}
	
	public FieldInfo(long[] as, long[] bs, long[] cs, long[] ds, long[] es, long[] fs) {
		// ThinkFirePossibility で呼ばれるコンストラクタ
		long[] tempfield = {as[0], bs[0], cs[0], ds[0], es[0], fs[0]};
		long[] tempojama = {as[1], bs[1], cs[1], ds[1], es[1], fs[1]};
		field = tempfield;
		ojama = tempojama;
		score = 0;
		nrensa = 0;
	}
	
	public FieldInfo(long a, long b, long c, long d, long e, long f) {
		// EvaluateFieldFast のおじゃまがない場合で呼ばれるコンストラクタ
		long[] tempfield = {a, b, c, d, e, f};
		field = tempfield;
		ojama = new long[6];
		score = 0;
		nrensa = 0;
	}
	
	public FieldInfo(long[] x) {
		// おじゃまがない場合のChokudaiSearchで最後の評価の時に呼ばれるコンストラクタ
		field = x;
		ojama = new long[6];
		score = 0;
		nrensa = 0;
	}
	
	
	public FieldInfo(long a, long b, long c, long d, long e, long f, long ojamaa, long ojamab, long ojamac, long ojamad, long ojamae, long ojamaf) {
		// ThinkFirePossibility で呼ばれるコンストラクタ
		long[] tempfield = {a, b, c, d, e, f};
		long[] tempojama = {ojamaa, ojamab, ojamac, ojamad, ojamae, ojamaf};
		field = tempfield;
		ojama = tempojama;
		score = 0;
		nrensa = 0;
	}
	
	public FieldInfo(FieldInfo fieldinfo, int first, int second) {
		// Tsumos で呼ばれるコンストラクタ
		field = Arrays.copyOf(fieldinfo.field, 6);
		ojama = Arrays.copyOf(fieldinfo.ojama, 6);
		score = 0;
		nrensa = 0;
		firstpuyo = first;
		secondpuyo = second;
		ojamaList = fieldinfo.ojamaList;
	}
	
	public FieldInfo(FieldInfo fieldinfo, int first, int second, int firstcolumn, int secondcolumn, int[] top, boolean isup) {
		field = Arrays.copyOf(fieldinfo.field, 6);
		ojama = Arrays.copyOf(fieldinfo.ojama, 6);
		if (firstcolumn == secondcolumn) {

			if (isup) {
				field[firstcolumn] |= (long)1 << (first + top[firstcolumn]); 
				field[secondcolumn] |= (long)1 << (second + top[secondcolumn] + 5); 
			}
			else {
				field[firstcolumn] |= (long)1 << (first + top[firstcolumn] + 5); 
				field[secondcolumn] |= (long)1 << (second + top[secondcolumn]); 
			}
		}
		else {
			field[firstcolumn] |= (long)1 << (first + top[firstcolumn]); 
			field[secondcolumn] |= (long)1 << (second + top[secondcolumn]); 
		}
		score = 0;
		nrensa = 0;
	}
	
	public FieldInfo[] AvailableFields() {
		FieldInfo[] output = new FieldInfo[fieldafteravailableactions.length];
		for (int i=0;i<fieldafteravailableactions.length;i++) {
			output[i] = new FieldInfo();
			output[i].field = Arrays.copyOf(fieldafteravailableactions[i], 6);
			output[i].ojama = Arrays.copyOf(ojama, 6);
			output[i].firstpuyo = this.nextfirstpuyo;
			output[i].secondpuyo = this.nextsecondpuyo;
			output[i].nextfirstpuyo = this.nextnextfirstpuyo;
			output[i].nextsecondpuyo = this.nextnextsecondpuyo;
			if (this.ojamaList.length <= 1) {
				output[i].ojamaList = new int[1];
			}
			else {
				output[i].ojamaList = Arrays.copyOfRange(this.ojamaList, 1, this.ojamaList.length);
			}
			output[i].scoreCarry = this.scoreCarry;
			output[i].firstActionIndex = this.firstActionIndex;
		}
		return output;
	}
}
