package meow2021;

import java.util.Arrays;
import java.util.Random;

public class FastBitNextFieldNoOjama {
	// 盤面におじゃまがなく、おじゃまが降ってもこない場合の盤面を処理するクラス

	public int[] topindextable;
	public int[] topindextableb5;
	public int[][][][][][][][][] availableactionstable; // 大きさ(3*3*3*3*3*3*(0~11)*2, 3*3*3*3*3*3*(0~22)*2) = 48114(以下) (puyoをrotateさせる回数、置く場所)の組
	public int[][][][][][][][][] availableputplacestable; // （firstpuyoを置く場所、secondpuyoを置く場所）の組が入っている
	// PuyoDirection が Down のとき(availableactionstable[][][][][][][][][0] == 2 のとき)はsecondpuyoを先に置かないといけない。
	public int[] toptoindex;
	public Np np;
	private Random rd;
	public FastBitNextField bnf;
	
	
	public  FastBitNextFieldNoOjama() {
		this.np = new Np();
		this.rd = new Random();
		
		// topindexを返す関数で参照するテーブルを作る
		topindextable = new int[64];
		topindextableb5 = new int[64];
		long hash = 0x03F566ED27179461L;
		for ( int i = 0; i < 64; i++ )
		{
		    topindextable[(int)( hash >>> 58) & 0x3F] = (i + 5)/ 5;
		    topindextableb5[(int)( hash >>> 58) & 0x3F] = ((i + 5)/ 5) * 5;
		    hash <<= 1;
		}

		// 次の行動を返す時に参照するテーブルを作る
		availableactionstable = new int[2][3][3][3][3][3][3][][];
		availableputplacestable = new int[2][3][3][3][3][3][3][][];
		toptoindex = new int[13];
		int[][] samecoloravacts = new int[11][2];
		int[][] samecolorptplcs = new int[11][2];
		int[][] difcoloravacts = new int[22][2];
		int[][] difcolorptplcs = new int[22][2];
		toptoindex[12] = 2;
		toptoindex[11] = 1;
		samecoloravacts[0][0] = 0;
		samecoloravacts[0][1] = 0;
		samecolorptplcs[0][0] = 0;
		samecolorptplcs[0][1] = 0;
		difcoloravacts[0][0] = 0;
		difcoloravacts[0][1] = 0;
		difcolorptplcs[0][0] = 0;
		difcolorptplcs[0][1] = 0;
		difcoloravacts[1][0] = 2;
		difcoloravacts[1][1] = 0;
		difcolorptplcs[1][0] = 0;
		difcolorptplcs[1][1] = 0;
		for (int i=0;i<5;i++) {
			samecoloravacts[i+1][0] = 0;
			samecoloravacts[i+1][1] = i+1;
			samecoloravacts[i+6][0] = 1;
			samecoloravacts[i+6][1] = i;
			samecolorptplcs[i+1][0] = i+1;
			samecolorptplcs[i+1][1] = i+1;
			samecolorptplcs[i+6][0] = i;
			samecolorptplcs[i+6][1] = i+1;

			difcoloravacts[i * 2 + 2][0] = 0;
			difcoloravacts[i * 2 + 2][1] = i+1;
			difcoloravacts[i * 2 + 3][0] = 2;
			difcoloravacts[i * 2 + 3][1] = i+1;
			difcoloravacts[i * 2 + 12][0] = 1;
			difcoloravacts[i * 2 + 12][1] = i;
			difcoloravacts[i * 2 + 13][0] = 3;
			difcoloravacts[i * 2 + 13][1] = i+1;
			difcolorptplcs[i * 2 + 2][0] = i+1;
			difcolorptplcs[i * 2 + 2][1] = i+1;
			difcolorptplcs[i * 2 + 3][0] = i+1;
			difcolorptplcs[i * 2 + 3][1] = i+1;
			difcolorptplcs[i * 2 + 12][0] = i;
			difcolorptplcs[i * 2 + 12][1] = i+1;
			difcolorptplcs[i * 2 + 13][0] = i+1;
			difcolorptplcs[i * 2 + 13][1] = i;
		}
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				for (int k=0;k<3;k++) {
					for (int l=0;l<3;l++) {
						for (int m=0;m<3;m++) {
							for (int n=0;n<3;n++) {
								boolean[] useplaces = new boolean[11];
								if (i == 0) {
									useplaces[0] = true;
								}
								if (j == 0) {
									useplaces[1] = true;
								}
								if (k == 0) {
									useplaces[2] = true;
								}
								if (l == 0) {
									useplaces[3] = true;
								}
								if (m == 0) {
									useplaces[4] = true;
								}
								if (n == 0) {
									useplaces[5] = true;
								}
								if (i < 2 && j < 2) {
									useplaces[6] = true;
								}
								if (j < 2 && k < 2) {
									useplaces[7] = true;
								}
								if (k < 2 && l < 2) {
									useplaces[8] = true;
								}
								if (l < 2 && m < 2) {
									useplaces[9] = true;
								}
								if (m < 2 && n < 2) {
									useplaces[10] = true;
								}
								int tempactionnum = 0;
								for (int o=0;o<11;o++) {
									if (useplaces[o]) {
										tempactionnum++;
									}
								}
								int[][] sactions = new int[tempactionnum][2];
								int[][] splaces = new int[tempactionnum][2];
								int[][] dactions = new int[tempactionnum * 2][2];
								int[][] dplaces = new int[tempactionnum * 2][2];
								int tempactionnum2 = 0;
								for (int o=0;o<11;o++) {
									if (useplaces[o]) {
										sactions[tempactionnum2] = samecoloravacts[o];
										splaces[tempactionnum2] = samecolorptplcs[o];
										dactions[tempactionnum2 * 2] = difcoloravacts[o * 2];
										dactions[tempactionnum2 * 2 + 1] = difcoloravacts[o * 2 + 1];
										dplaces[tempactionnum2 * 2] = difcolorptplcs[o * 2];
										dplaces[tempactionnum2 * 2 + 1] = difcolorptplcs[o * 2 + 1];
										tempactionnum2++;
									}
								}
								availableactionstable[0][i][j][k][l][m][n] = sactions;
								availableactionstable[1][i][j][k][l][m][n] = dactions;
								availableputplacestable[0][i][j][k][l][m][n] = splaces;
								availableputplacestable[1][i][j][k][l][m][n] = dplaces;
							}
						}
					}
				}
			}
		}
	}
	
	public double ChokudaiSearch(FieldInfo inputfield, int[][] tsumo, int searchwidth) {
		int searchdepth = tsumo.length;
		FieldInfoQueueNoOjama[] queues = new FieldInfoQueueNoOjama[searchdepth + 1];
		for (int i=0;i<searchdepth + 1;i++) {
			queues[i] = new FieldInfoQueueNoOjama(this);
		}
		long[] firstfield = new long[7];
		for (int i=0;i<6;i++) {
			firstfield[i] = inputfield.field[i];
		}
		queues[0].add(firstfield);
		double evaluation = -10;
		// chokudaiSearchでツモに対する最善の最初のターンの動きとその評価値を求める
		for (int n=0;n<searchwidth;n++) {
			for (int i=0;i<searchdepth;i++) {
				// i ... 現在からの経過ターン数
				if (queues[i].isEmpty()) {
					// このターンの盤面をすべて探索して、
					// 次のターンにあり得る盤面全てを次のターンのqueueに追加し終えていたら
					continue;
				}
				// 指定ターン目のキューにたまっている盤面のうち、最も評価値が高い盤面を取る
				long[] field = queues[i].poll();
				
				double tempeva =  Double.longBitsToDouble(field[6]);
				evaluation = evaluation < tempeva ? tempeva : evaluation;
				// ツモの動かし方最大22通りにfieldを分岐させる
				long[][] nextfields = ThinkNextActionsAndCalc(field, tsumo[i]);
				// int blankbefore = CountBlank(field);
				for (long[] nextfield: nextfields) {
					// 評価値を計算してキューに追加
					// 一度見たものはFieldInfoQueue内部のmHashに記録されていて、add()を使っても追加されない
					queues[i + 1].add(nextfield);
					/*
					if (CountBlank(nextfield) < blankbefore + 6) {
						// ぷよを8個以上消す発火をしていなかったら
						queues[i + 1].add(nextfield);
					}
					*/
				}
			}
		}
		
		long[] lastfield = queues[searchdepth].poll();
		double tempeva = lastfield == null ? 0 : Double.longBitsToDouble(lastfield[6]);
		evaluation = evaluation < tempeva ? tempeva : evaluation;
		/*
		for (int n=0;n<searchwidth;n++) {
			long[] lastfield = queues[searchdepth - 1].poll();
			if (lastfield != null) {
				evaluation += Double.longBitsToDouble(lastfield[6]);
				addcount++;
			}
			else {
				break;
			}
		}
		evaluation /= addcount == 0 ? 1 : addcount;
		*/
		return evaluation;
	}
	

	public double[] ChokudaiSearch2(FieldInfo[] inputfields, int[][] tsumo, int searchwidth) {
		int searchdepth = tsumo.length;
		FieldInfoQueueNoOjama[] queues = new FieldInfoQueueNoOjama[searchdepth];
		for (int i=0;i<searchdepth;i++) {
			queues[i] = new FieldInfoQueueNoOjama(this);
		}
		
		long[][] firstfields = new long[inputfields.length][8]; // 盤面6列、評価値、最初の動きのインデックスの計8個
		for (int i=0;i<inputfields.length;i++) {
			for (int j=0;j<6;j++) {
				firstfields[i][j] = inputfields[i].field[j];
			}
			firstfields[i][7] = i;
			queues[0].add(firstfields[i]);
		}
		// chokudaiSearchでツモに対する最善の最初のターンの動きとその評価値を求める
		double[] maxevaluations = new double[inputfields.length];
		for (int n=0;n<searchwidth;n++) {
			for (int i=0;i<searchdepth;i++) {
				// i ... 現在からの経過ターン数
				if (queues[i].isEmpty()) {
					// このターンの盤面をすべて探索して、
					// 次のターンにあり得る盤面全てを次のターンのqueueに追加し終えていたら
					continue;
				}
				// 指定ターン目のキューにたまっている盤面のうち、最も評価値が高い盤面を取る
				long[] field = queues[i].poll();
				double tempeva = Double.longBitsToDouble(field[6]);
				double beforeeva = maxevaluations[(int) field[7]];
				maxevaluations[(int) field[7]] = tempeva > beforeeva ? tempeva : beforeeva;
				if (i != searchdepth - 1) {
					int blankbefore = CountBlank(field);
					
					// ツモの動かし方最大22通りにfieldを分岐させる
					long[][] nextfields = ThinkNextActionsAndCalc(field, tsumo[i]);
					for (long[] nextfield: nextfields) {
						// 評価値を計算してキューに追加
						// 一度見たものはFieldInfoQueue内部のmHashに記録されていて、add()を使っても追加されない
						if (CountBlank(nextfield) < blankbefore + 6) {
							// ぷよを8個以上消す発火をしていなかったら
							queues[i + 1].add(nextfield);
						}
					}
				}
			}
		}
		return maxevaluations;
	}
	
	public int CountBlank(long[] field) {
		return 72 - (TopIndex(field[0]) + TopIndex(field[1]) + TopIndex(field[2]) + TopIndex(field[3]) + TopIndex(field[4]) + TopIndex(field[5]));
	}
	
	public double EvaluateFieldFast(long[] field) {
		long a = field[0];
		long b = field[1];
		long c = field[2];
		long d = field[3];
		long e = field[4];
		long f = field[5];
		// まず同じ色のぷよがつながっている辺の数を計算する
		long mask = 37191016277640225L;
		
		long am = a & b;
		long bm = b & c;
		long cm = c & d;
		long dm = d & e;
		long em = e & f;
		long aue1 = a >>> 5;
		long bue1 = b >>> 5;
		long cue1 = c >>> 5;
		long due1 = d >>> 5;
		long eue1 = e >>> 5;
		long fue1 = f >>> 5;
		long au = a & aue1;
		long bu = b & bue1;
		long cu = c & cue1;
		long du = d & due1;
		long eu = e & eue1;
		long fu = f & fue1;
		long tempam = am | (am >>> 1) | (am >>> 2);
		long amigi = (tempam | (tempam >>> 2)) & mask;
		long tempau = au | (au >>> 1) | (au >>> 2);
		long aue = (tempau | (tempau >>> 2)) & mask;
		long tempbm = bm | (bm >>> 1) | (bm >>> 2);
		long bmigi = (tempbm | (tempbm >>> 2)) & mask;
		long tempbu = bu | (bu >>> 1) | (bu >>> 2);
		long bue = (tempbu | (tempbu >>> 2)) & mask;
		long tempcm = cm | (cm >>> 1) | (cm >>> 2);
		long cmigi = (tempcm | (tempcm >>> 2)) & mask;
		long tempcu = cu | (cu >>> 1) | (cu >>> 2);
		long cue = (tempcu | (tempcu >>> 2)) & mask;
		long tempdm = dm | (dm >>> 1) | (dm >>> 2);
		long dmigi = (tempdm | (tempdm >>> 2)) & mask;
		long tempdu = du | (du >>> 1) | (du >>> 2);
		long due = (tempdu | (tempdu >>> 2)) & mask;
		long tempem = em | (em >>> 1) | (em >>> 2);
		long emigi = (tempem | (tempem >>> 2)) & mask;
		long tempeu = eu | (eu >>> 1) | (eu >>> 2);
		long eue = (tempeu | (tempeu >>> 2)) & mask;
		long tempfu = fu | (fu >>> 1) | (fu >>> 2);
		long fue = (tempfu | (tempfu >>> 2)) & mask;
		
		long tempconnect = amigi | (bmigi << 1) | (cmigi << 2) | (dmigi << 3) | (emigi << 4);
		long tempconnect2 = aue | (bue << 1) | (cue << 2) | (due << 3) | (eue << 4);
		int connectnum = Long.bitCount(tempconnect) + Long.bitCount(tempconnect2) + Long.bitCount(fue);

		// 表面のぷよを消したときの最大連鎖数を計算する
		long atoprised = TopRised(a);
		long btoprised = TopRised(b);
		long ctoprised = TopRised(c);
		long dtoprised = TopRised(d);
		long etoprised = TopRised(e);
		long ftoprised = TopRised(f);
		
		int atop = TopIndexb5(a);
		int btop = TopIndexb5(b);
		int ctop = TopIndexb5(c);
		int dtop = TopIndexb5(d);
		int etop = TopIndexb5(e);
		int ftop = TopIndexb5(f);
		
		// 表面の場所を取り出す
		long afilled = atoprised == 0 ? 0 : ((~atoprised) ^ (-atoprised)) & mask;
		long bfilled = btoprised == 0 ? 0 : ((~btoprised) ^ (-btoprised)) & mask;
		long cfilled = ctoprised == 0 ? 0 : ((~ctoprised) ^ (-ctoprised)) & mask;
		long dfilled = dtoprised == 0 ? 0 : ((~dtoprised) ^ (-dtoprised)) & mask;
		long efilled = etoprised == 0 ? 0 : ((~etoprised) ^ (-etoprised)) & mask;
		long ffilled = ftoprised == 0 ? 0 : ((~ftoprised) ^ (-ftoprised)) & mask;
		long asurface = (afilled & (~bfilled)) | (0x1L << (atop - 5));
		long bsurface = (bfilled & (~(afilled & cfilled))) | (0x1L << (btop - 5));
		long csurface = (cfilled & (~(bfilled & dfilled))) | (0x1L << (ctop - 5));
		long dsurface = (dfilled & (~(cfilled & efilled))) | (0x1L << (dtop- 5));
		long esurface = (efilled & (~(dfilled & ffilled))) | (0x1L << (etop - 5));
		long fsurface = (ffilled & (~efilled)) | (0x1L << (ftop - 5));
		
		long[] maxrensafield = new long[6];
		// 各表面のぷよについてそれぞれ消してみる
		double rensaevaluation = 0;
		while (asurface != 0) {
			// 表面のぷよの位置を一つ取り出す
			long erasea = asurface & (-asurface);

			// つながっているのが最大３ぷよなので、周囲25マスを見る
			erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 一つ上と一つ下を見る
			erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 二つ上から二つ下までを見る
			long eraseb = erasea & amigi;
			long erasec = eraseb & bmigi;

			// 既に消したところは、今後消してみる位置のリストから外す
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;

			// 発火に必要なぷよの数を調べる
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2)); 
			if (fourminustempnumtofire != 3){
				// 後2つ以上ぷよが必要だったら
				continue;
			}
			
			// ぷよを落とす
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			// 連鎖を計算する
			long[] tempfi = {a2, b2, c2, d, e, f};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		
		while (bsurface != 0) {
			long eraseb = bsurface & (-bsurface);
			eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
			eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
			long erasea = eraseb & amigi;
			long erasec = eraseb & bmigi;
			long erased = erasec & cmigi;
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3));
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			if (fourminustempnumtofire != 3) continue;
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long[] tempfi = {a2, b2, c2, d2, e, f};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		
		while (csurface != 0) {
			long erasec = csurface & (-csurface);
			erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
			erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
			long eraseb = erasec & bmigi;
			long erasea = eraseb & amigi;
			long erased = erasec & cmigi;
			long erasee = erased & dmigi;
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3) | (erasee << 4));
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			if (fourminustempnumtofire != 3) continue;  
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long[] tempfi = {a2, b2, c2, d2, e2, f};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		
		while (dsurface != 0) {
			long erased = dsurface & (-dsurface);
			erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
			erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
			long erasec = erased & cmigi;
			long eraseb = erasec & bmigi;
			long erasee = erased & dmigi;
			long erasef = erasee & emigi;
			int fourminustempnumtofire = Long.bitCount(eraseb | (erasec << 1) | (erased << 2) | (erasee << 3) | (erasef << 4));
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue; 
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b2, c2, d2, e2, f2};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		
		while (esurface != 0) {
			long erasee = esurface & (-esurface);
			erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
			erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
			long erased = erasee & dmigi;
			long erasec = erased & cmigi;
			long erasef = erasee & emigi;
			int fourminustempnumtofire = Long.bitCount(erasec | (erased << 1) | (erasee << 2) | (erasef << 3));
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue;
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b, c2, d2, e2, f2};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		
		while (fsurface != 0) {
			long erasef = fsurface & (-fsurface);
			erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
			erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
			long erasee = erasef & emigi;
			long erased = erasee & dmigi;		
			int fourminustempnumtofire = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue;
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b, c, d2, e2, f2};
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			rensaevaluation = rensaevaluation > nrensa ? rensaevaluation : nrensa;
			if (nrensa > rensaevaluation) {
				maxrensafield = Arrays.copyOf(tempfi, 6);
			}
		}
		// 2 + 0.3 * (ぷよの数)に近い 
		double evaluation = rensaevaluation  + (connectnum) * 0.2;
		
		// evaluation += (CountConnect(maxrensafield) - CountPuyo(maxrensafield)) * 0.2;
		// evaluation += CountPuyo(maxrensafield) * 0.1;
		/*
		 if (CountPuyo(maxrensafield) > 10) {
			 evaluation += EvaluateFieldFast(maxrensafield) * 0.5;
		 }
		 */
		return evaluation;
	}
	
	public int CountPuyo(long[] field) {
		int puyocount = 0;
		for (int i=0;i<6;i++) {
			puyocount += TopIndex(field[i]);
		}
		return puyocount;
	}
	
	public double EvaluateFieldFastwithScore(long[] field) {
		// 点数ベースで評価する
		long a = field[0];
		long b = field[1];
		long c = field[2];
		long d = field[3];
		long e = field[4];
		long f = field[5];
		// まず同じ色のぷよがつながっている辺の数を計算する
		long mask = 37191016277640225L;
		
		long am = a & b;
		long bm = b & c;
		long cm = c & d;
		long dm = d & e;
		long em = e & f;
		long aue1 = a >>> 5;
		long bue1 = b >>> 5;
		long cue1 = c >>> 5;
		long due1 = d >>> 5;
		long eue1 = e >>> 5;
		long fue1 = f >>> 5;
		long au = a & aue1;
		long bu = b & bue1;
		long cu = c & cue1;
		long du = d & due1;
		long eu = e & eue1;
		long fu = f & fue1;
		long tempam = am | (am >>> 1) | (am >>> 2);
		long amigi = (tempam | (tempam >>> 2)) & mask;
		long tempau = au | (au >>> 1) | (au >>> 2);
		long aue = (tempau | (tempau >>> 2)) & mask;
		long tempbm = bm | (bm >>> 1) | (bm >>> 2);
		long bmigi = (tempbm | (tempbm >>> 2)) & mask;
		long tempbu = bu | (bu >>> 1) | (bu >>> 2);
		long bue = (tempbu | (tempbu >>> 2)) & mask;
		long tempcm = cm | (cm >>> 1) | (cm >>> 2);
		long cmigi = (tempcm | (tempcm >>> 2)) & mask;
		long tempcu = cu | (cu >>> 1) | (cu >>> 2);
		long cue = (tempcu | (tempcu >>> 2)) & mask;
		long tempdm = dm | (dm >>> 1) | (dm >>> 2);
		long dmigi = (tempdm | (tempdm >>> 2)) & mask;
		long tempdu = du | (du >>> 1) | (du >>> 2);
		long due = (tempdu | (tempdu >>> 2)) & mask;
		long tempem = em | (em >>> 1) | (em >>> 2);
		long emigi = (tempem | (tempem >>> 2)) & mask;
		long tempeu = eu | (eu >>> 1) | (eu >>> 2);
		long eue = (tempeu | (tempeu >>> 2)) & mask;
		long tempfu = fu | (fu >>> 1) | (fu >>> 2);
		long fue = (tempfu | (tempfu >>> 2)) & mask;
		
		long tempconnect = amigi | (bmigi << 1) | (cmigi << 2) | (dmigi << 3) | (emigi << 4);
		long tempconnect2 = aue | (bue << 1) | (cue << 2) | (due << 3) | (eue << 4);
		int connectnum = Long.bitCount(tempconnect) + Long.bitCount(tempconnect2) + Long.bitCount(fue);

		// 表面のぷよを消したときの最大連鎖数を計算する
		long atoprised = TopRised(a);
		long btoprised = TopRised(b);
		long ctoprised = TopRised(c);
		long dtoprised = TopRised(d);
		long etoprised = TopRised(e);
		long ftoprised = TopRised(f);
		// 表面の場所を取り出す
		long afilled = atoprised == 0 ? 0 : ((~atoprised) ^ (-atoprised)) & mask;
		long bfilled = btoprised == 0 ? 0 : ((~btoprised) ^ (-btoprised)) & mask;
		long cfilled = ctoprised == 0 ? 0 : ((~ctoprised) ^ (-ctoprised)) & mask;
		long dfilled = dtoprised == 0 ? 0 : ((~dtoprised) ^ (-dtoprised)) & mask;
		long efilled = etoprised == 0 ? 0 : ((~etoprised) ^ (-etoprised)) & mask;
		long ffilled = ftoprised == 0 ? 0 : ((~ftoprised) ^ (-ftoprised)) & mask;
		long asurface = (afilled & (~bfilled)) | (0x1L << (TopIndexb5(a) - 5));
		long bsurface = (bfilled & (~(afilled & cfilled))) | (0x1L << (TopIndexb5(b) - 5));
		long csurface = (cfilled & (~(bfilled & dfilled))) | (0x1L << (TopIndexb5(c) - 5));
		long dsurface = (dfilled & (~(cfilled & efilled))) | (0x1L << (TopIndexb5(d) - 5));
		long esurface = (efilled & (~(dfilled & ffilled))) | (0x1L << (TopIndexb5(e) - 5));
		long fsurface = (ffilled & (~efilled)) | (0x1L << (TopIndexb5(f) - 5));
		
		// 各表面のぷよについてそれぞれ消してみる
		double rensaevaluation = 0;
		while (asurface != 0) {
			// 表面のぷよの位置を一つ取り出す
			long erasea = asurface & (-asurface);

			// つながっているのが最大３ぷよなので、周囲25マスを見る
			erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 一つ上と一つ下を見る
			erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 二つ上から二つ下までを見る
			long eraseb = erasea & amigi;
			long erasec = eraseb & bmigi;

			// 既に消したところは、今後消してみる位置のリストから外す
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;

			// 発火に必要なぷよの数を調べる
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2)); 
			if (fourminustempnumtofire != 3){
				// 後2つ以上ぷよが必要だったら
				continue;
			}
			
			// ぷよを落とす
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			// 連鎖を計算する
			long[] tempfi = {a2, b2, c2, d, e, f};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		
		while (bsurface != 0) {
			long eraseb = bsurface & (-bsurface);
			eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
			eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
			long erasea = eraseb & amigi;
			long erasec = eraseb & bmigi;
			long erased = erasec & cmigi;
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3));
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			if (fourminustempnumtofire != 3) continue;
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long[] tempfi = {a2, b2, c2, d2, e, f};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		
		while (csurface != 0) {
			long erasec = csurface & (-csurface);
			erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
			erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
			long eraseb = erasec & bmigi;
			long erasea = eraseb & amigi;
			long erased = erasec & cmigi;
			long erasee = erased & dmigi;
			int fourminustempnumtofire = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3) | (erasee << 4));
			asurface ^= asurface & erasea;
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			if (fourminustempnumtofire != 3) continue;  
			long a2 = FallDownPuyo(a, erasea);
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long[] tempfi = {a2, b2, c2, d2, e2, f};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		
		while (dsurface != 0) {
			long erased = dsurface & (-dsurface);
			erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
			erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
			long erasec = erased & cmigi;
			long eraseb = erasec & bmigi;
			long erasee = erased & dmigi;
			long erasef = erasee & emigi;
			int fourminustempnumtofire = Long.bitCount(eraseb | (erasec << 1) | (erased << 2) | (erasee << 3) | (erasef << 4));
			bsurface ^= bsurface & eraseb;
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue; 
			long b2 = FallDownPuyo(b, eraseb);
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b2, c2, d2, e2, f2};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		
		while (esurface != 0) {
			long erasee = esurface & (-esurface);
			erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
			erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
			long erased = erasee & dmigi;
			long erasec = erased & cmigi;
			long erasef = erasee & emigi;
			int fourminustempnumtofire = Long.bitCount(erasec | (erased << 1) | (erasee << 2) | (erasef << 3));
			csurface ^= csurface & erasec;
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue;
			long c2 = FallDownPuyo(c, erasec);
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b, c2, d2, e2, f2};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		
		while (fsurface != 0) {
			long erasef = fsurface & (-fsurface);
			erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
			erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
			long erasee = erasef & emigi;
			long erased = erasee & dmigi;		
			int fourminustempnumtofire = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
			dsurface ^= dsurface & erased;
			esurface ^= esurface & erasee;
			fsurface ^= fsurface & erasef;
			if (fourminustempnumtofire != 3) continue;
			long d2 = FallDownPuyo(d, erased);
			long e2 = FallDownPuyo(e, erasee);
			long f2 = FallDownPuyo(f, erasef);
			long[] tempfi = {a, b, c, d2, e2, f2};
			double tempscore = ApproxScore(tempfi);
			rensaevaluation = rensaevaluation > tempscore ? rensaevaluation : tempscore;
		}
		 // 同列以外の本来の順位に影響を与えない程度にランダム要素を入れる
		// さもないとqueues.poll()したときに点数が同じ場合最初に追加されたものが返されるため、
		// actionindex = 0 の行動の点数ばかりが高くなってしまう
		double evaluation = rensaevaluation + (connectnum) * 0.2;
		
		return evaluation;
	}
	
	public double ApproxScore(long[] field) {
		// 1連鎖目が起こった後の状態を入力とする
		// スコアを概算する
		double score = 40;
		int rensa = 1;
		int[] erasenum = new int[1];
		while (RensaNext(field, erasenum)) {
			rensa++;
			int rensabonus = rensa < 4 ? (rensa - 1) * 8 : (rensa - 3) * 32;
			int otherbonus = erasenum[0] - 4;
			score += erasenum[0] * (rensabonus + otherbonus) * 10;
		}
		return score;
	}
	
	public long TopRised(long data) {
		if (data == 0) {
			return 0;
		}
		else {
			data = (data & 0xFFFFFFFF00000000L) != 0 ? (data & 0xFFFFFFFF00000000L) : data;
			data = (data & 0xFFFF0000FFFF0000L) != 0 ? (data & 0xFFFF0000FFFF0000L) : data;
			data = (data & 0xFF00FF00FF00FF00L) != 0 ? (data & 0xFF00FF00FF00FF00L) : data;
			data = (data & 0xF0F0F0F0F0F0F0F0L) != 0 ? (data & 0xF0F0F0F0F0F0F0F0L) : data;
			data = (data & 0xCCCCCCCCCCCCCCCCL) != 0 ? (data & 0xCCCCCCCCCCCCCCCCL) : data;
			data = (data & 0xAAAAAAAAAAAAAAAAL) != 0 ? (data & 0xAAAAAAAAAAAAAAAAL) : data;
			return data;
		}
	}
	
	public int TopIndex(long data) {
		if (data == 0) {
			return 0;
		}
		else {
			data = (data & 0xFFFFFFFF00000000L) != 0 ? (data & 0xFFFFFFFF00000000L) : data;
			data = (data & 0xFFFF0000FFFF0000L) != 0 ? (data & 0xFFFF0000FFFF0000L) : data;
			data = (data & 0xFF00FF00FF00FF00L) != 0 ? (data & 0xFF00FF00FF00FF00L) : data;
			data = (data & 0xF0F0F0F0F0F0F0F0L) != 0 ? (data & 0xF0F0F0F0F0F0F0F0L) : data;
			data = (data & 0xCCCCCCCCCCCCCCCCL) != 0 ? (data & 0xCCCCCCCCCCCCCCCCL) : data;
			data = (data & 0xAAAAAAAAAAAAAAAAL) != 0 ? (data & 0xAAAAAAAAAAAAAAAAL) : data;
			// magic number を用いて右からn個目の位置のビットのみが立っているlongに対してnを求める
			long index = (((data * 0x03F566ED27179461L) >>> 58) & 0x3F);
			return topindextable[(int) index];
		}
	}
	
	public int TopIndexb5(long data) {
		if (data == 0) {
			return 0;
		}
		else {
			data = (data & 0xFFFFFFFF00000000L) != 0 ? (data & 0xFFFFFFFF00000000L) : data;
			data = (data & 0xFFFF0000FFFF0000L) != 0 ? (data & 0xFFFF0000FFFF0000L) : data;
			data = (data & 0xFF00FF00FF00FF00L) != 0 ? (data & 0xFF00FF00FF00FF00L) : data;
			data = (data & 0xF0F0F0F0F0F0F0F0L) != 0 ? (data & 0xF0F0F0F0F0F0F0F0L) : data;
			data = (data & 0xCCCCCCCCCCCCCCCCL) != 0 ? (data & 0xCCCCCCCCCCCCCCCCL) : data;
			data = (data & 0xAAAAAAAAAAAAAAAAL) != 0 ? (data & 0xAAAAAAAAAAAAAAAAL) : data;
			// magic number を用いて右からn個目の位置のビットのみが立っているlongに対してnを求める
			long index = (((data * 0x03F566ED27179461L) >>> 58) & 0x3F);
			return topindextableb5[(int) index];
		}
	}
	
	public long FallDownPuyo(long inputx, long eraseplace) {
		while (eraseplace != 0) {
			long temperase = eraseplace & (-eraseplace);
			long moveplace = -temperase;
			long saveplace = ~moveplace;
			inputx = inputx >>> 5 & moveplace | (inputx & saveplace);
			eraseplace ^= temperase;
			eraseplace >>>= 5;
		}
		return inputx;
	}
	
	public boolean RensaNext(long[] field) {
		// CalcNextとほぼ同じ内容だが、FieldInfo型を経由しない。
		// また、スコアを計算せず、連鎖が続くかのみを見る。
		// さらにおじゃまが盤面にないことを前提にしている。
		// まず周囲4つとつながっているかを見る
		long a = field[0];
		long b = field[1];
		long c = field[2];
		long d = field[3];
		long e = field[4];
		long f = field[5];
		
		long am = a & b;
		long bm = b & c;
		long cm = c & d;
		long dm = d & e;
		long em = e & f;
		long au = a & (a >>> 5);
		long bu = b & (b >>> 5);
		long cu = c & (c >>> 5);
		long du = d & (d >>> 5);
		long eu = e & (e >>> 5);
		long fu = f & (f >>> 5);
		long mask = 37191016277640225L; // 最初と、そこから5ビットごとに1が立っている　61ビット目は例外的に0
		
		long tempam = am | (am >>> 1) | (am >>> 2);
		long amigi = (tempam | (tempam >>> 2)) & mask;
		long tempau = au | (au >>> 1) | (au >>> 2);
		long aue = (tempau | (tempau >>> 2)) & mask;
		long tempbm = bm | (bm >>> 1) | (bm >>> 2);
		long bmigi = (tempbm | (tempbm >>> 2)) & mask;
		long tempbu = bu | (bu >>> 1) | (bu >>> 2);
		long bue = (tempbu | (tempbu >>> 2)) & mask;
		long tempcm = cm | (cm >>> 1) | (cm >>> 2);
		long cmigi = (tempcm | (tempcm >>> 2)) & mask;
		long tempcu = cu | (cu >>> 1) | (cu >>> 2);
		long cue = (tempcu | (tempcu >>> 2)) & mask;
		long tempdm = dm | (dm >>> 1) | (dm >>> 2);
		long dmigi = (tempdm | (tempdm >>> 2)) & mask;
		long tempdu = du | (du >>> 1) | (du >>> 2);
		long due = (tempdu | (tempdu >>> 2)) & mask;
		long tempem = em | (em >>> 1) | (em >>> 2);
		long emigi = (tempem | (tempem >>> 2)) & mask;
		long tempeu = eu | (eu >>> 1) | (eu >>> 2);
		long eue = (tempeu | (tempeu >>> 2)) & mask;
		long tempfu = fu | (fu >>> 1) | (fu >>> 2);
		long fue = (tempfu | (tempfu >>> 2)) & mask;
		
		// 次に、4つつながっている場所を探す。
		// 5種類すべてのテトラミノは、ドミノの周囲6か所の内2か所に
		// 正方形がつながっている形で表せるので、この条件を満たすドミノをまず見つける。
		
		long tempamigi = amigi + (amigi >>> 5);
		long tempbmigi = bmigi + (bmigi >>> 5);
		long tempcmigi = cmigi + (cmigi >>> 5);
		long tempdmigi = dmigi + (dmigi >>> 5);
		long tempemigi = emigi + (emigi >>> 5);

		long atatedomino = (aue << 5) + (aue >>> 5) + tempamigi;
		long btatedomino = (bue << 5) + (bue >>> 5) + tempamigi + tempbmigi;
		long ctatedomino = (cue << 5) + (cue >>> 5) + tempbmigi + tempcmigi;
		long dtatedomino = (due << 5) + (due >>> 5) + tempcmigi + tempdmigi;
		long etatedomino = (eue << 5) + (eue >>> 5) + tempdmigi + tempemigi;
		long ftatedomino = (fue << 5) + (fue >>> 5) + tempemigi;
		
		atatedomino = ((atatedomino >>> 1) | (atatedomino >>> 2)) & aue;
		btatedomino = ((btatedomino >>> 1) | (btatedomino >>> 2)) & bue;
		ctatedomino = ((ctatedomino >>> 1) | (ctatedomino >>> 2)) & cue;
		dtatedomino = ((dtatedomino >>> 1) | (dtatedomino >>> 2)) & due;
		etatedomino = ((etatedomino >>> 1) | (etatedomino >>> 2)) & eue;
		ftatedomino = ((ftatedomino >>> 1) | (ftatedomino >>> 2)) & fue;
		
		long tempaue = aue + (aue << 5);
		long tempbue = bue + (bue << 5);
		long tempcue = cue + (cue << 5);
		long tempdue = due + (due << 5);
		long tempeue = eue + (eue << 5);
		long tempfue = fue + (fue << 5);
		
		long ayokodomino = bmigi + tempaue + tempbue;
		long byokodomino = amigi + cmigi + tempbue + tempcue;
		long cyokodomino = bmigi + dmigi + tempcue + tempdue;
		long dyokodomino = cmigi + emigi + tempdue + tempeue;
		long eyokodomino = dmigi + tempeue + tempfue;
		
		ayokodomino = ((ayokodomino >>> 1) | (ayokodomino >>> 2)) & amigi;
		byokodomino = ((byokodomino >>> 1) | (byokodomino >>> 2)) & bmigi;
		cyokodomino = ((cyokodomino >>> 1) | (cyokodomino >>> 2)) & cmigi;
		dyokodomino = ((dyokodomino >>> 1) | (dyokodomino >>> 2)) & dmigi;
		eyokodomino = ((eyokodomino >>> 1) | (eyokodomino >>> 2)) & emigi;
		
		// テトラミノ中のドミノを見つけたところで、そこを消える場所として記録する
		
		long erasea = atatedomino | (atatedomino << 5) | ayokodomino;
		long eraseb = btatedomino | (btatedomino << 5) | ayokodomino | byokodomino;	
		long erasec = ctatedomino | (ctatedomino << 5) | byokodomino | cyokodomino;
		long erased = dtatedomino | (dtatedomino << 5) | cyokodomino | dyokodomino;
		long erasee = etatedomino | (etatedomino << 5) | dyokodomino | eyokodomino;
		long erasef = ftatedomino | (ftatedomino << 5) | eyokodomino;

		// 消える場所とつながっている場所を消える場所として記録する
		
		erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5) | (eraseb & amigi);
		eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5) | (erasea & amigi) | (erasec & bmigi);
		erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5) | (eraseb & bmigi) | (erased & cmigi);
		erased |= ((erased >>> 5) & due) | ((erased & due) << 5) | (erasec & cmigi) | (erasee & dmigi);
		erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5) | (erased & dmigi) | (erasef & emigi);
		erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5) | (erasee & emigi);
		

		if ((erasea | eraseb | erasec | erased | erasee | erasef) == 0) {
			return false;
		}
		else {
			// ぷよを落とす
			while (erasea != 0) {
				long temperase = erasea & (-erasea);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = a & moveplace;
				mv &= mv - 1;
				a = (mv >>> 5) | (a & saveplace);
				erasea ^= temperase;
				erasea >>>= 5;
			}
			while (eraseb != 0) {
				long temperase = eraseb & (-eraseb);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = b & moveplace;
				mv &= mv - 1;
				b = (mv >>> 5) | (b & saveplace);
				eraseb ^= temperase;
				eraseb >>>= 5;
			}
			while (erasec != 0) {
				long temperase = erasec & (-erasec);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = c & moveplace;
				mv &= mv - 1;
				c = (mv >>> 5) | (c & saveplace);
				erasec ^= temperase;
				erasec >>>= 5;
			}
			while (erased != 0) {
				long temperase = erased & (-erased);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = d & moveplace;
				mv &= mv - 1;
				d = (mv >>> 5) | (d & saveplace);
				erased ^= temperase;
				erased >>>= 5;
			}
			while (erasee != 0) {
				long temperase = erasee & (-erasee);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = e & moveplace;
				mv &= mv - 1;
				e = (mv >>> 5) | (e & saveplace);
				erasee ^= temperase;
				erasee >>>= 5;
			}
			while (erasef != 0) {
				long temperase = erasef & (-erasef);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = f & moveplace;
				mv &= mv - 1;
				f = (mv >>> 5) | (f & saveplace);
				erasef ^= temperase;
				erasef >>>= 5;
			}
			// fieldの値を書き換える
			field[0] = a;
			field[1] = b;
			field[2] = c;
			field[3] = d;
			field[4] = e;
			field[5] = f;
			return true;
		}
	}
	

	public boolean RensaNext(long[] field, int[] outerasenum) {
		// CalcNextとほぼ同じ内容だが、FieldInfo型を経由しない。
		// また、おじゃまが盤面にないことを前提にしている。
		// 消えた個数を計算してouterasenum[0]に入れる
		// まず周囲4つとつながっているかを見る
		long a = field[0];
		long b = field[1];
		long c = field[2];
		long d = field[3];
		long e = field[4];
		long f = field[5];
		
		long am = a & b;
		long bm = b & c;
		long cm = c & d;
		long dm = d & e;
		long em = e & f;
		long au = a & (a >>> 5);
		long bu = b & (b >>> 5);
		long cu = c & (c >>> 5);
		long du = d & (d >>> 5);
		long eu = e & (e >>> 5);
		long fu = f & (f >>> 5);
		long mask = 37191016277640225L; // 最初と、そこから5ビットごとに1が立っている　61ビット目は例外的に0
		
		long tempam = am | (am >>> 1) | (am >>> 2);
		long amigi = (tempam | (tempam >>> 2)) & mask;
		long tempau = au | (au >>> 1) | (au >>> 2);
		long aue = (tempau | (tempau >>> 2)) & mask;
		long tempbm = bm | (bm >>> 1) | (bm >>> 2);
		long bmigi = (tempbm | (tempbm >>> 2)) & mask;
		long tempbu = bu | (bu >>> 1) | (bu >>> 2);
		long bue = (tempbu | (tempbu >>> 2)) & mask;
		long tempcm = cm | (cm >>> 1) | (cm >>> 2);
		long cmigi = (tempcm | (tempcm >>> 2)) & mask;
		long tempcu = cu | (cu >>> 1) | (cu >>> 2);
		long cue = (tempcu | (tempcu >>> 2)) & mask;
		long tempdm = dm | (dm >>> 1) | (dm >>> 2);
		long dmigi = (tempdm | (tempdm >>> 2)) & mask;
		long tempdu = du | (du >>> 1) | (du >>> 2);
		long due = (tempdu | (tempdu >>> 2)) & mask;
		long tempem = em | (em >>> 1) | (em >>> 2);
		long emigi = (tempem | (tempem >>> 2)) & mask;
		long tempeu = eu | (eu >>> 1) | (eu >>> 2);
		long eue = (tempeu | (tempeu >>> 2)) & mask;
		long tempfu = fu | (fu >>> 1) | (fu >>> 2);
		long fue = (tempfu | (tempfu >>> 2)) & mask;
		
		// 次に、4つつながっている場所を探す。
		// 5種類すべてのテトラミノは、ドミノの周囲6か所の内2か所に
		// 正方形がつながっている形で表せるので、この条件を満たすドミノをまず見つける。
		
		long tempamigi = amigi + (amigi >>> 5);
		long tempbmigi = bmigi + (bmigi >>> 5);
		long tempcmigi = cmigi + (cmigi >>> 5);
		long tempdmigi = dmigi + (dmigi >>> 5);
		long tempemigi = emigi + (emigi >>> 5);

		long atatedomino = (aue << 5) + (aue >>> 5) + tempamigi;
		long btatedomino = (bue << 5) + (bue >>> 5) + tempamigi + tempbmigi;
		long ctatedomino = (cue << 5) + (cue >>> 5) + tempbmigi + tempcmigi;
		long dtatedomino = (due << 5) + (due >>> 5) + tempcmigi + tempdmigi;
		long etatedomino = (eue << 5) + (eue >>> 5) + tempdmigi + tempemigi;
		long ftatedomino = (fue << 5) + (fue >>> 5) + tempemigi;
		
		atatedomino = ((atatedomino >>> 1) | (atatedomino >>> 2)) & aue;
		btatedomino = ((btatedomino >>> 1) | (btatedomino >>> 2)) & bue;
		ctatedomino = ((ctatedomino >>> 1) | (ctatedomino >>> 2)) & cue;
		dtatedomino = ((dtatedomino >>> 1) | (dtatedomino >>> 2)) & due;
		etatedomino = ((etatedomino >>> 1) | (etatedomino >>> 2)) & eue;
		ftatedomino = ((ftatedomino >>> 1) | (ftatedomino >>> 2)) & fue;
		
		long tempaue = aue + (aue << 5);
		long tempbue = bue + (bue << 5);
		long tempcue = cue + (cue << 5);
		long tempdue = due + (due << 5);
		long tempeue = eue + (eue << 5);
		long tempfue = fue + (fue << 5);
		
		long ayokodomino = bmigi + tempaue + tempbue;
		long byokodomino = amigi + cmigi + tempbue + tempcue;
		long cyokodomino = bmigi + dmigi + tempcue + tempdue;
		long dyokodomino = cmigi + emigi + tempdue + tempeue;
		long eyokodomino = dmigi + tempeue + tempfue;
		
		ayokodomino = ((ayokodomino >>> 1) | (ayokodomino >>> 2)) & amigi;
		byokodomino = ((byokodomino >>> 1) | (byokodomino >>> 2)) & bmigi;
		cyokodomino = ((cyokodomino >>> 1) | (cyokodomino >>> 2)) & cmigi;
		dyokodomino = ((dyokodomino >>> 1) | (dyokodomino >>> 2)) & dmigi;
		eyokodomino = ((eyokodomino >>> 1) | (eyokodomino >>> 2)) & emigi;
		
		// テトラミノ中のドミノを見つけたところで、そこを消える場所として記録する
		
		long erasea = atatedomino | (atatedomino << 5) | ayokodomino;
		long eraseb = btatedomino | (btatedomino << 5) | ayokodomino | byokodomino;	
		long erasec = ctatedomino | (ctatedomino << 5) | byokodomino | cyokodomino;
		long erased = dtatedomino | (dtatedomino << 5) | cyokodomino | dyokodomino;
		long erasee = etatedomino | (etatedomino << 5) | dyokodomino | eyokodomino;
		long erasef = ftatedomino | (ftatedomino << 5) | eyokodomino;

		// 消える場所とつながっている場所を消える場所として記録する
		
		erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5) | (eraseb & amigi);
		eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5) | (erasea & amigi) | (erasec & bmigi);
		erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5) | (eraseb & bmigi) | (erased & cmigi);
		erased |= ((erased >>> 5) & due) | ((erased & due) << 5) | (erasec & cmigi) | (erasee & dmigi);
		erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5) | (erased & dmigi) | (erasef & emigi);
		erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5) | (erasee & emigi);
		

		if ((erasea | eraseb | erasec | erased | erasee | erasef) == 0) {
			return false;
		}
		else {
			int erasecount1 = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2));
			int erasecount2 = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
			outerasenum[0] = erasecount1 + erasecount2;
			// ぷよを落とす
			while (erasea != 0) {
				long temperase = erasea & (-erasea);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = a & moveplace;
				mv &= mv - 1;
				a = (mv >>> 5) | (a & saveplace);
				erasea ^= temperase;
				erasea >>>= 5;
			}
			while (eraseb != 0) {
				long temperase = eraseb & (-eraseb);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = b & moveplace;
				mv &= mv - 1;
				b = (mv >>> 5) | (b & saveplace);
				eraseb ^= temperase;
				eraseb >>>= 5;
			}
			while (erasec != 0) {
				long temperase = erasec & (-erasec);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = c & moveplace;
				mv &= mv - 1;
				c = (mv >>> 5) | (c & saveplace);
				erasec ^= temperase;
				erasec >>>= 5;
			}
			while (erased != 0) {
				long temperase = erased & (-erased);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = d & moveplace;
				mv &= mv - 1;
				d = (mv >>> 5) | (d & saveplace);
				erased ^= temperase;
				erased >>>= 5;
			}
			while (erasee != 0) {
				long temperase = erasee & (-erasee);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = e & moveplace;
				mv &= mv - 1;
				e = (mv >>> 5) | (e & saveplace);
				erasee ^= temperase;
				erasee >>>= 5;
			}
			while (erasef != 0) {
				long temperase = erasef & (-erasef);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = f & moveplace;
				mv &= mv - 1;
				f = (mv >>> 5) | (f & saveplace);
				erasef ^= temperase;
				erasef >>>= 5;
			}
			// fieldの値を書き換える
			field[0] = a;
			field[1] = b;
			field[2] = c;
			field[3] = d;
			field[4] = e;
			field[5] = f;
			return true;
		}
	}
	
	 public long[][] ThinkNextActionsAndCalc(long[] field, int[] currenttsumo) {
	    	// 既に連鎖が計算された後の盤面を入力として、
	    	// 次のツモの置き方すべてについて、
	    	// ツモを置いた後の盤面を返す
	    	long[][] nextfields = ThinkNextActions(field, currenttsumo);	
	    	for (int i = 0; i < nextfields.length; i++) {
	    		while (RensaNext(nextfields[i]));
	    	}
	    	return nextfields;
	    }
		
		public long[][] ThinkNextActions(long[] field, int[] currenttsumo) {
			// 可能手の探索
			long a = field[0];
			long b = field[1];
			long c = field[2];
			long d = field[3];
			long e = field[4];
			long f = field[5];
			int atop = TopIndex(a); // この列に何もなかったら0, 1段目にあったら1
			int btop = TopIndex(b);
			int ctop = TopIndex(c);
			int dtop = TopIndex(d);
			int etop = TopIndex(e);
			int ftop = TopIndex(f);
			int at = toptoindex[atop];
			int bt = toptoindex[btop];
			int ct = toptoindex[ctop];
			int dt = toptoindex[dtop];
			int et = toptoindex[etop];
			int ft = toptoindex[ftop];
			// 可能手の探索で使うのは一番上が10段目以下か、11段目か、12段目かの3通りと、
			// 次のぷよが2つとも同じ色かどうかのみなので、
			// 3^6 * 2 = 1458通りの場合における行動を記したテーブルを作って、それを参照する。
			int[][] putplaces;
			int[][] availableactions;
			long[][] fieldafteravailableactions;
			int nextfirstpuyo = currenttsumo[0];
			int nextsecondpuyo = currenttsumo[1];
			if (nextfirstpuyo == nextsecondpuyo) {
				availableactions = availableactionstable[0][at][bt][ct][dt][et][ft];
				putplaces = availableputplacestable[0][at][bt][ct][dt][et][ft];
			}
			else {
				availableactions = availableactionstable[1][at][bt][ct][dt][et][ft];
				putplaces = availableputplacestable[1][at][bt][ct][dt][et][ft];
			}
			long[] tempfield = {a, b, c, d, e, f};
			fieldafteravailableactions = new long[availableactions.length][field.length]; // 横に6列、さらに評価値を保存する場所1つでlong[7]
			// atopb5m1 = atop * 5 - 1
			//                  = atop * 4 + atop * 2 + (-atop - 1)
			//                  = (atop << 2) + (atop << 1) + (~atop)
			// あと足し算を分解すればもう少し早くなるかもしれない
			int atopb5m1 = (atop <<  2) + (atop << 1) + (~atop);
			int btopb5m1 = (btop <<  2) + (btop << 1) + (~btop);
			int ctopb5m1 = (ctop <<  2) + (ctop << 1) + (~ctop);
			int dtopb5m1 = (dtop <<  2) + (dtop << 1) + (~dtop);
			int etopb5m1 = (etop <<  2) + (etop << 1) + (~etop);
			int ftopb5m1 = (ftop <<  2) + (ftop << 1) + (~ftop);
			int[] temptops = {atopb5m1, btopb5m1, ctopb5m1, dtopb5m1, etopb5m1, ftopb5m1};
			for (int i=0;i<availableactions.length;i++) {
				if (field.length >= 8) {
					fieldafteravailableactions[i][7] = field[7]; // この盤面につながる最初の行動のインデックスを引き継ぐ
				}
				for (int j=0;j<6;j++) {
					fieldafteravailableactions[i][j] = tempfield[j];
				}
				if (availableactions[i][0] == 2) {
					// secondpuyoを先に置くとき
					fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextsecondpuyo)) | ((long)1 << (temptops[putplaces[i][0]] + 5 + nextfirstpuyo));
				}
				else if (availableactions[i][0] == 0) {
					// firstpuyoを先に置くとき
					fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextfirstpuyo)) | ((long)1 << (temptops[putplaces[i][1]] + 5 + nextsecondpuyo));
				}
				else {
					// 横に置くとき
					fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextfirstpuyo));
					fieldafteravailableactions[i][putplaces[i][1]] |= ((long)1 << (temptops[putplaces[i][1]] + nextsecondpuyo));
				}
			}
			return fieldafteravailableactions;
		}
		
		public double EvaluateFieldFastPuttingTsumo(long[] field) {
			// 置くと発火する場所をリストアップする
			// 自分・相手それぞれの3つのツモを置いた後の全状態について呼び出したとき、
			// 最初のターンのみ80ms、以降1ターンあたり10ms程度かかる
			
			// 各列の最初のビットを取り出す
			long a = field[0];
			long b = field[1];
			long c = field[2];
			long d = field[3];
			long e = field[4];
			long f = field[5];
			long mask = 37191016277640225L;
			long atoprised = TopRised(a);
			long btoprised = TopRised(b);
			long ctoprised = TopRised(c);
			long dtoprised = TopRised(d);
			long etoprised = TopRised(e);
			long ftoprised = TopRised(f);
			// 表面の場所を取り出す
			long afilled = atoprised == 0 ? 0 : ((~atoprised) ^ (-atoprised)) & mask;
			long bfilled = btoprised == 0 ? 0 : ((~btoprised) ^ (-btoprised)) & mask;
			long cfilled = ctoprised == 0 ? 0 : ((~ctoprised) ^ (-ctoprised)) & mask;
			long dfilled = dtoprised == 0 ? 0 : ((~dtoprised) ^ (-dtoprised)) & mask;
			long efilled = etoprised == 0 ? 0 : ((~etoprised) ^ (-etoprised)) & mask;
			long ffilled = ftoprised == 0 ? 0 : ((~ftoprised) ^ (-ftoprised)) & mask;
			
			long putamask = ((atoprised * 62) & mask) * 0x1F; // aの上1段分のビットだけが1になったマスク
			long putbmask = ((btoprised * 62) & mask) * 0x1F; 
			long putcmask = ((ctoprised * 62) & mask) * 0x1F; 
			long putdmask = ((dtoprised * 62) & mask) * 0x1F; 
			long putemask = ((etoprised * 62) & mask) * 0x1F;
			long putfmask = ((ftoprised * 62) & mask) * 0x1F; 
			
			long asurface = (afilled & putbmask) | ((long)1 << (TopIndexb5(a) - 5));
			long bsurface = (bfilled & (putamask | putcmask)) | ((long)1 << (TopIndexb5(b) - 5));
			long csurface = (cfilled & (putbmask | putdmask)) | ((long)1 << (TopIndexb5(c) - 5));
			long dsurface = (dfilled & (putcmask | putemask)) | ((long)1 << (TopIndexb5(d) - 5));
			long esurface = (efilled & (putdmask | putfmask)) | ((long)1 << (TopIndexb5(e) - 5));
			long fsurface = (ffilled & putemask) | ((long)1 << (TopIndexb5(f) - 5));
			
			// 後でつながっているぷよを消すために、どこがつながっているかを見る
			long aue1 = a >>> 5;
			long bue1 = b >>> 5;
			long cue1 = c >>> 5;
			long due1 = d >>> 5;
			long eue1 = e >>> 5;
			long fue1 = f >>> 5;
			long am = a & b;
			long bm = b & c;
			long cm = c & d;
			long dm = d & e;
			long em = e & f;
			long au = a & (aue1);
			long bu = b & (bue1);
			long cu = c & (cue1);
			long du = d & (due1);
			long eu = e & (eue1);
			long fu = f & (fue1);
			long tempam = am | (am >>> 1) | (am >>> 2);
			long amigi = (tempam | (tempam >>> 2)) & mask;
			long tempau = au | (au >>> 1) | (au >>> 2);
			long aue = (tempau | (tempau >>> 2)) & mask;
			long tempbm = bm | (bm >>> 1) | (bm >>> 2);
			long bmigi = (tempbm | (tempbm >>> 2)) & mask;
			long tempbu = bu | (bu >>> 1) | (bu >>> 2);
			long bue = (tempbu | (tempbu >>> 2)) & mask;
			long tempcm = cm | (cm >>> 1) | (cm >>> 2);
			long cmigi = (tempcm | (tempcm >>> 2)) & mask;
			long tempcu = cu | (cu >>> 1) | (cu >>> 2);
			long cue = (tempcu | (tempcu >>> 2)) & mask;
			long tempdm = dm | (dm >>> 1) | (dm >>> 2);
			long dmigi = (tempdm | (tempdm >>> 2)) & mask;
			long tempdu = du | (du >>> 1) | (du >>> 2);
			long due = (tempdu | (tempdu >>> 2)) & mask;
			long tempem = em | (em >>> 1) | (em >>> 2);
			long emigi = (tempem | (tempem >>> 2)) & mask;
			long tempeu = eu | (eu >>> 1) | (eu >>> 2);
			long eue = (tempeu | (tempeu >>> 2)) & mask;
			long tempfu = fu | (fu >>> 1) | (fu >>> 2);
			long fue = (tempfu | (tempfu >>> 2)) & mask;
			

			// 次に、間にぷよを一つはさんで同じ色のぷよが縦に2つ離れて並んでいる場所の数を計算する
			long aue2 = a & (a >>> 10);
			long bue2 = b & (b >>> 10);
			long cue2 = c & (c >>> 10);
			long due2 = d & (d >>> 10);
			long eue2 = e & (e >>> 10);
			long fue2 = f & (f >>> 10);
			long tempau2 = aue2 | (aue2 >>> 1) | (aue2 >>> 2);
			long au2 = (tempau2 | (tempau2 >>> 2)) & mask;
			long tempbu2 = bue2 | (bue2 >>> 1) | (bue2 >>> 2);
			long bu2 = (tempbu2 | (tempbu2 >>> 2)) & mask;
			long tempcu2 = cue2 | (cue2 >>> 1) | (cue2 >>> 2);
			long cu2 = (tempcu2 | (tempcu2 >>> 2)) & mask;
			long tempdu2 = due2 | (due2 >>> 1) | (due2 >>> 2);
			long du2 = (tempdu2 | (tempdu2 >>> 2)) & mask;
			long tempeu2 = eue2 | (eue2 >>> 1) | (eue2 >>> 2);
			long eu2 = (tempeu2 | (tempeu2 >>> 2)) & mask;
			long tempfu2 = fue2 | (fue2 >>> 1) | (fue2 >>> 2);
			long fu2 = (tempfu2 | (tempfu2 >>> 2)) & mask;
			
			long tempue1 = au2 | (bu2 << 1) | (cu2 << 2);
			long tempue2 = du2 | (eu2 << 1) | (fu2 << 2);
			int ue2num = Long.bitCount(tempue1) + Long.bitCount(tempue2);
			
			// 同色のぷよが斜めに配置されている場所の数を数える
			long ananame1 = a & bue1;
			long bnaname1 = b & cue1;
			long cnaname1 = c & due1;
			long dnaname1 = d & eue1;
			long enaname1 = e & fue1;
			long ananame2 = b & aue1;
			long bnaname2 = c & bue1;
			long cnaname2 = d & cue1;
			long dnaname2 = e & due1;
			long enaname2 = f & eue1;
			
			long tempan1 = ananame1 | (ananame1 >>> 1) | (ananame1 >>> 2);
			long an1 = (tempan1 | (tempan1 >>> 2)) & mask;
			long tempbn1 = bnaname1 | (bnaname1 >>> 1) | (bnaname1 >>> 2);
			long bn1 = (tempbn1 | (tempbn1 >>> 2)) & mask;
			long tempcn1 = cnaname1 | (cnaname1 >>> 1) | (cnaname1 >>> 2);
			long cn1 = (tempcn1 | (tempcn1 >>> 2)) & mask;
			long tempdn1 = dnaname1 | (dnaname1 >>> 1) | (dnaname1 >>> 2);
			long dn1 = (tempdn1 | (tempdn1 >>> 2)) & mask;
			long tempen1 = enaname1 | (enaname1 >>> 1) | (enaname1 >>> 2);
			long en1 = (tempen1 | (tempen1 >>> 2)) & mask;
			long tempan2 = ananame2 | (ananame2 >>> 1) | (ananame2 >>> 2);
			long an2 = (tempan2 | (tempan2 >>> 2)) & mask;
			long tempbn2 = bnaname2 | (bnaname2 >>> 1) | (bnaname2 >>> 2);
			long bn2 = (tempbn2 | (tempbn2 >>> 2)) & mask;
			long tempcn2 = cnaname2 | (cnaname2 >>> 1) | (cnaname2 >>> 2);
			long cn2 = (tempcn2 | (tempcn2 >>> 2)) & mask;
			long tempdn2 = dnaname2 | (dnaname2 >>> 1) | (dnaname2 >>> 2);
			long dn2 = (tempdn2 | (tempdn2 >>> 2)) & mask;
			long tempen2 = enaname2 | (enaname2 >>> 1) | (enaname2 >>> 2);
			long en2 = (tempen2 | (tempen2 >>> 2)) & mask;
			
			long naname1 = an1 | (bn1 << 1) | (cn1 << 2) | (dn1 << 3) | (en1 << 4);
			long naname2 = an2 | (bn2 << 1) | (cn2 << 2) | (dn2 << 3) | (en2 << 4);
			int nanamenum = Long.bitCount(naname1) + Long.bitCount(naname2);
			
			 // a列の表面にあるぷよで、（他の列にまたがっていてもよいので）3個ぷよがつながっている場所
			// ビット位置で色も表している
			long connect3erasea = 0;
			long connect3eraseb = 0;
			long connect3erasec = 0;
			long connect3erased = 0;
			long connect3erasee = 0;
			long connect3erasef = 0;
			
			
			while (asurface != 0) {
				// 表面のぷよの位置を一つ取り出す
				long erasea = asurface & (-asurface);
				
				// ぷよの色を確認する
				// 色の確認はもっと簡単にできそう
				long temp = (erasea * 31) & a; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);

				// つながっているのが最大３ぷよなので、周囲25マスを見る
				erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 一つ上と一つ下を見る
				erasea |= ((erasea >>> 5) & aue) | ((erasea & aue) << 5); // 二つ上から二つ下までを見る
				long eraseb = erasea & amigi;
				long erasec = eraseb & bmigi;
				
				// 発火に必要なぷよの数を調べる
				int connectnum = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2));

				// 既に消したところは、今後消してみる位置のリストから外す
				asurface ^= asurface & erasea;
				bsurface ^= bsurface & eraseb;
				csurface ^= csurface & erasec;
				
				if (connectnum == 3) {
					connect3erasea |= erasea * erasecolor;
					connect3eraseb |= eraseb * erasecolor;
					connect3erasec |= erasec * erasecolor;
				}
			}
			
			while (bsurface != 0) {
				long eraseb = bsurface & (-bsurface);
				long temp = (eraseb * 31) & b; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);
				eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
				eraseb |= ((eraseb >>> 5) & bue) | ((eraseb & bue) << 5);
				long erasea = eraseb & amigi;
				long erasec = eraseb & bmigi;
				long erased = erasec & cmigi;
				int connectnum = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3));
				asurface ^= asurface & erasea;
				bsurface ^= bsurface & eraseb;
				csurface ^= csurface & erasec;
				dsurface ^= dsurface & erased;
				if (connectnum == 3) {
					connect3erasea |= erasea * erasecolor;
					connect3eraseb |= eraseb * erasecolor;
					connect3erasec |= erasec * erasecolor;
					connect3erased |= erased * erasecolor;
				}
			}
			
			while (csurface != 0) {
				long erasec = csurface & (-csurface);
				long temp = (erasec * 31) & c; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);
				erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
				erasec |= ((erasec >>> 5) & cue) | ((erasec & cue) << 5);
				long eraseb = erasec & bmigi;
				long erasea = eraseb & amigi;
				long erased = erasec & cmigi;
				long erasee = erased & dmigi;
				int connectnum = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2) | (erased << 3) | (erasee << 4));
				asurface ^= asurface & erasea;
				bsurface ^= bsurface & eraseb;
				csurface ^= csurface & erasec;
				dsurface ^= dsurface & erased;
				esurface ^= esurface & erasee;
				if (connectnum == 3) {
					connect3erasea |= erasea * erasecolor;
					connect3eraseb |= eraseb * erasecolor;
					connect3erasec |= erasec * erasecolor;
					connect3erased |= erased * erasecolor;
					connect3erasee |= erasee * erasecolor;
				}
			}
			
			while (dsurface != 0) {
				long erased = dsurface & (-dsurface);
				long temp = (erased * 31) & d; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);
				erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
				erased |= ((erased >>> 5) & due) | ((erased & due) << 5);
				long erasec = erased & cmigi;
				long eraseb = erasec & bmigi;
				long erasee = erased & dmigi;
				long erasef = erasee & emigi;
				int connectnum = Long.bitCount(eraseb | (erasec << 1) | (erased << 2) | (erasee << 3) | (erasef << 4));
				bsurface ^= bsurface & eraseb;
				csurface ^= csurface & erasec;
				dsurface ^= dsurface & erased;
				esurface ^= esurface & erasee;
				fsurface ^= fsurface & erasef;
				if (connectnum == 3) {
					connect3eraseb |= eraseb * erasecolor;
					connect3erasec |= erasec * erasecolor;
					connect3erased |= erased * erasecolor;
					connect3erasee |= erasee * erasecolor;
					connect3erasef |= erasef * erasecolor;
				}
			}
			
			while (esurface != 0) {
				long erasee = esurface & (-esurface);
				long temp = (erasee * 31) & e; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);
				erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
				erasee |= ((erasee >>> 5) & eue) | ((erasee & eue) << 5);
				long erased = erasee & dmigi;
				long erasec = erased & cmigi;
				long erasef = erasee & emigi;
				int connectnum = Long.bitCount(erasec | (erased << 1) | (erasee << 2) | (erasef << 3));
				csurface ^= csurface & erasec;
				dsurface ^= dsurface & erased;
				esurface ^= esurface & erasee;
				fsurface ^= fsurface & erasef;
				if (connectnum == 3) {
					connect3erasec |= erasec * erasecolor;
					connect3erased |= erased * erasecolor;
					connect3erasee |= erasee * erasecolor;
					connect3erasef |= erasef * erasecolor;
				}
			}
			
			while (fsurface != 0) {
				long erasef = fsurface & (-fsurface);
				long temp = (erasef * 31) & f; 
				long erasecolor = temp >>> (TopIndexb5(temp) - 5);
				erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
				erasef |= ((erasef >>> 5) & fue) | ((erasef & fue) << 5);
				long erasee = erasef & emigi;
				long erased = erasee & dmigi;		
				int connectnum = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
				dsurface ^= dsurface & erased;
				esurface ^= esurface & erasee;
				fsurface ^= fsurface & erasef;
				if (connectnum == 3) {
					connect3erased |= erased * erasecolor;
					connect3erasee |= erasee * erasecolor;
					connect3erasef |= erasef * erasecolor;
				}
			}
			boolean[][] placetofire1= new boolean[5][6]; // 1個で発火できる場所
			boolean[][] placetofire2= new boolean[5][6]; // 2個で発火できる場所


			long putacolor = (((connect3erasea << 5) | connect3erasec) & putamask) >>> TopIndexb5(a);
			long putbcolor = ((connect3erasea | (connect3eraseb << 5) | connect3erasec) & putbmask) >>> TopIndexb5(b);
			long putccolor = ((connect3eraseb | (connect3erasec << 5) | connect3erased) & putcmask) >>> TopIndexb5(c);
			long putdcolor = ((connect3erasec | (connect3erased << 5) | connect3erasee) & putdmask) >>> TopIndexb5(d);
			long putecolor = ((connect3erased | (connect3erasee << 5) | connect3erasef) & putemask) >>> TopIndexb5(e);
			long putfcolor = ((connect3erasee | (connect3erasef << 5)) & putfmask) >>> TopIndexb5(f);
			
			putacolor |= (putacolor >>> 5);
			putbcolor |= (putbcolor >>> 5);
			putccolor |= (putccolor >>> 5);
			putdcolor |= (putdcolor >>> 5);
			putecolor |= (putecolor >>> 5);
			putfcolor |= (putfcolor >>> 5);
	
			boolean[] putacolorarr = bnf.LongBitstoBooleanArray(putacolor, 5);
			boolean[] putbcolorarr = bnf.LongBitstoBooleanArray(putbcolor, 5);
			boolean[] putccolorarr = bnf.LongBitstoBooleanArray(putccolor, 5);
			boolean[] putdcolorarr = bnf.LongBitstoBooleanArray(putdcolor, 5);
			boolean[] putecolorarr = bnf.LongBitstoBooleanArray(putecolor, 5);
			boolean[] putfcolorarr = bnf.LongBitstoBooleanArray(putfcolor, 5);
			
			int nrensa = 0;
			for (int i=0;i<5;i++) {
				if (putacolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 0, i));
				}
			}
			for (int i=0;i<5;i++) {
				if (putbcolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 1, i));
				}
			}
			for (int i=0;i<5;i++) {
				if (putccolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 2, i));
				}
			}
			for (int i=0;i<5;i++) {
				if (putdcolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 3, i));
				}
			}
			for (int i=0;i<5;i++) {
				if (putecolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 4, i));
				}
			}
			for (int i=0;i<5;i++) {
				if (putfcolorarr[i]) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, 5, i));
				}
			}
			long tempconnect = amigi | (bmigi << 1) | (cmigi << 2) | (dmigi << 3) | (emigi << 4);
			long tempconnect2 = aue | (bue << 1) | (cue << 2) | (due << 3) | (eue << 4);
			int connectnum = Long.bitCount(tempconnect) + Long.bitCount(tempconnect2) + Long.bitCount(fue);
			return nrensa + connectnum * 0.08 + nanamenum * 0.01 + ue2num * 0.01;
		}
		
		private int PutaPuyoAndCalcNumRensa(long[] field, int column, int puyocolor) {
			// puyocolorは0オリジン
			long[] tempfi = Arrays.copyOf(field, field.length);
			int top = TopIndexb5(tempfi[column]);
			tempfi[column] |= (0x1L << puyocolor) << top;
			int nrensa = 0;
			while (RensaNext(tempfi)) nrensa++;
			return nrensa;
		}
		
		public double EvaluateFieldFastPuttingTsumo2(long[] field) {
			// こちらの方が遅い
			int nrensa = 0;
			for (int i=0;i<6;i++) {
				for (int j=0;j<5;j++) {
					nrensa = Math.max(nrensa, PutaPuyoAndCalcNumRensa(field, i, j));
				}
			}
			return nrensa + CountConnect(field) * 0.2;
		}
		
		public int CountConnect(long[] field) {
			long a = field[0];
			long b = field[1];
			long c = field[2];
			long d = field[3];
			long e = field[4];
			long f = field[5];
			// まず同じ色のぷよがつながっている辺の数を計算する
			long mask = 37191016277640225L;
			
			long am = a & b;
			long bm = b & c;
			long cm = c & d;
			long dm = d & e;
			long em = e & f;
			long aue1 = a >>> 5;
			long bue1 = b >>> 5;
			long cue1 = c >>> 5;
			long due1 = d >>> 5;
			long eue1 = e >>> 5;
			long fue1 = f >>> 5;
			long au = a & aue1;
			long bu = b & bue1;
			long cu = c & cue1;
			long du = d & due1;
			long eu = e & eue1;
			long fu = f & fue1;
			long tempam = am | (am >>> 1) | (am >>> 2);
			long amigi = (tempam | (tempam >>> 2)) & mask;
			long tempau = au | (au >>> 1) | (au >>> 2);
			long aue = (tempau | (tempau >>> 2)) & mask;
			long tempbm = bm | (bm >>> 1) | (bm >>> 2);
			long bmigi = (tempbm | (tempbm >>> 2)) & mask;
			long tempbu = bu | (bu >>> 1) | (bu >>> 2);
			long bue = (tempbu | (tempbu >>> 2)) & mask;
			long tempcm = cm | (cm >>> 1) | (cm >>> 2);
			long cmigi = (tempcm | (tempcm >>> 2)) & mask;
			long tempcu = cu | (cu >>> 1) | (cu >>> 2);
			long cue = (tempcu | (tempcu >>> 2)) & mask;
			long tempdm = dm | (dm >>> 1) | (dm >>> 2);
			long dmigi = (tempdm | (tempdm >>> 2)) & mask;
			long tempdu = du | (du >>> 1) | (du >>> 2);
			long due = (tempdu | (tempdu >>> 2)) & mask;
			long tempem = em | (em >>> 1) | (em >>> 2);
			long emigi = (tempem | (tempem >>> 2)) & mask;
			long tempeu = eu | (eu >>> 1) | (eu >>> 2);
			long eue = (tempeu | (tempeu >>> 2)) & mask;
			long tempfu = fu | (fu >>> 1) | (fu >>> 2);
			long fue = (tempfu | (tempfu >>> 2)) & mask;
			
			long tempconnect = amigi | (bmigi << 1) | (cmigi << 2) | (dmigi << 3) | (emigi << 4);
			long tempconnect2 = aue | (bue << 1) | (cue << 2) | (due << 3) | (eue << 4);
			int connectnum = Long.bitCount(tempconnect) + Long.bitCount(tempconnect2) + Long.bitCount(fue);
			return connectnum;
		}
}
