package meow2021;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Board;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Field;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo.PuyoNumber;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.storage.PuyoType;

public class FastBitNextField {
	// ビット演算で次のフィールドを考えるクラス
	public BiFunction<Integer, Integer, Double> firepossibilityevaluator;
	public double firepossibilityevaluation;
	public int[][][] scoretable;
	public int[] topindextable;
	public int[] topindextableb5;
	public int[][][][][][][][][] availableactionstable; // 大きさ(3*3*3*3*3*3*(0~11)*2, 3*3*3*3*3*3*(0~22)*2) = 48114(以下) (puyoをrotateさせる回数、置く場所)の組
	public int[][][][][][][][][] availableputplacestable; // （firstpuyoを置く場所、secondpuyoを置く場所）の組が入っている
	// PuyoDirection が Down のとき(availableactionstable[][][][][][][][][0] == 2 のとき)はsecondpuyoを先に置かないといけない。
	public int[] toptoindex;
	public double[] tsumoWeight;
	public Np np;
	
	public FastBitNextFieldNoOjama bnfno;
	
	public double sumtime;
	public double callcount;
	
	public  FastBitNextField() {
		// 各種テーブルなどの初期化を行う
		bnfno = new FastBitNextFieldNoOjama();
		bnfno.bnf = this;
		
		// まず得点を近似するテーブルの初期化を行う
		InitializeScoreTable();
		
		// ツモを記録する順番とそのツモが出る確率を対応させる
		this.tsumoWeight = TsumoWeight();
		
		this.np = new Np();
		
		// 表面にあるぷよを消したときの得点と、消すまでに必要なぷよの数から、評価値を出す関数を設定する
		double[] tempmulti = {0, 0.5, 0.25, 0.25};
		firepossibilityevaluator = (firepos, numtof) -> firepos * tempmulti[numtof];
		
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
		// これがないとtopindextable[0] が1になっているが、(0 * hash) >> 58 も(1 * hash) >> 58 も0になるので、
		// TopIndex() では特別に入力が0のときに0を返すようにする。
		
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

	public void InitializeScoreTable() {
		// 現在の連鎖数をx、x連鎖目で消したぷよの数をy、x連鎖目で消した色の数をzとしたとき、
		// x連鎖目での得点をthis.scoretable[x][y][z - 1] に格納する。
		// ただし全消しボーナスは含めない。
		// また、同時に4連結と6連結のぷよを消した時と、同時に5連結と5連結のぷよを消した場合を区別できないが、
		// 同じ色を同時に2か所以上で消した場合を除いて、あり得る最低の点数（1つを除いてすべて4連結とした場合の点数）を返す。
		int[] rensaBonus = {0, 0, 8, 16, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512};
		int[] colorNumBonus = {0, 0, 3, 6, 12, 24};
		Function<Integer, Integer> RenketsuBonus = (renketsuNum) -> {
			if (renketsuNum == 4) {
				return 0;
			}
			else if (renketsuNum < 11) {
				return renketsuNum - 3;
			}
			else {
				return 10;
			}
		};
		this.scoretable = new int[20][72][6];
		for (int rensaNum = 1;  rensaNum < 20; rensaNum++) {
			for (int eraseNum = 4;  eraseNum < 72; eraseNum++) {
				for (int eraseColorNum = 1;  eraseColorNum < 6; eraseColorNum++) {
					int tempRensaBonus = rensaBonus[rensaNum];
					int amariPuyoNum = eraseNum - eraseColorNum * 4;
					int tempRenketsuBonus = RenketsuBonus.apply(amariPuyoNum + 4);
					int tempColorNumBonus = colorNumBonus[eraseColorNum];
					int sumBonus = tempRensaBonus + tempRenketsuBonus + tempColorNumBonus;
					if (sumBonus == 0) {
						sumBonus = 1;
					}
					this.scoretable[rensaNum][eraseNum][eraseColorNum] = eraseNum * sumBonus * 10;
				}
			}
		}
	}
	
	public FieldInfo ReadField(Board board) {
		Field field = board.getField();
		FieldInfo output = new FieldInfo();
		for (int i=0;i<12;i++) {
			for (int j=0;j<6;j++) {
				if (field.getPuyoType(j, i) == PuyoType.OJAMA_PUYO) {
					output.ojama[j] |= (long)1 << (i * 5);
				}
				else {
					output.field[j] |= (long)ReadPuyoType(field.getPuyoType(j, i)) << (i * 5);
				}
			}
		}
		output.firstpuyo = IndexofPuyoType(board.getCurrentPuyo().getPuyoType(PuyoNumber.FIRST));
		output.secondpuyo = IndexofPuyoType(board.getCurrentPuyo().getPuyoType(PuyoNumber.SECOND));
		output.nextfirstpuyo = IndexofPuyoType(board.getNextPuyo().getPuyoType(PuyoNumber.FIRST));
		output.nextsecondpuyo = IndexofPuyoType(board.getNextPuyo().getPuyoType(PuyoNumber.SECOND));
		output.nextnextfirstpuyo = IndexofPuyoType(board.getNextNextPuyo().getPuyoType(PuyoNumber.FIRST));
		output.nextnextsecondpuyo = IndexofPuyoType(board.getNextNextPuyo().getPuyoType(PuyoNumber.SECOND));
		List<Integer>ojamaList = board.getNumbersOfOjamaList();
		int[] ojamaArray = new int[ojamaList.size()];
		for (int i=0;i<ojamaList.size();i++) {
			ojamaArray[i] = ojamaList.get(i);
		}
		output.ojamaList = ojamaArray;
		return output;
	}
	
	public int ReadPuyoType(PuyoType puyotype) {
		if (puyotype == null) {
			return 0;
		}
		else {
			switch (puyotype) {
			case BLUE_PUYO:
				return 1;
			case GREEN_PUYO:
				return 2;
			case PURPLE_PUYO:
				return 4;
			case RED_PUYO:
				return 8;
			case YELLOW_PUYO:
				return 16;
			default:
				return 0;
			}
		}
	}
	
	public int IndexofPuyoType(PuyoType puyotype) {
		if (puyotype == null) {
			return 0;
		}
		else {
			switch (puyotype) {
			case BLUE_PUYO:
				return 1;
			case GREEN_PUYO:
				return 2;
			case PURPLE_PUYO:
				return 3;
			case RED_PUYO:
				return 4;
			case YELLOW_PUYO:
				return 5;
			default:
				return 0;
			}
		}
	}
	
	public boolean CalcNext(FieldInfo field) {
		// まず周囲4つとつながっているかを見る
		long a = field.field[0];
		long b = field.field[1];
		long c = field.field[2];
		long d = field.field[3];
		long e = field.field[4];
		long f = field.field[5];
		long ojamaa = field.ojama[0];
		long ojamab = field.ojama[1];
		long ojamac = field.ojama[2];
		long ojamad = field.ojama[3];
		long ojamae = field.ojama[4];
		long ojamaf = field.ojama[5];
		
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
		
		// erasea~fをいじる前に得点を計算しておく
		int temperasenum1 = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2));
		int temperasenum2 = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
		int erasenum = temperasenum1 + temperasenum2;
		if (erasenum == 0) {
			return false;
		}
		else {
			long eraseamask = erasea * 31;
			long erasebmask = eraseb * 31;
			long erasecmask = erasec * 31;
			long erasedmask = erased * 31;
			long eraseemask = erasee * 31;
			long erasefmask = erasef * 31;
			
			// 消える色の数を見る
			/*// これは間違い　消える色の上に青色（1番目）のぷよがあると、青色も消える色に含めてしまう
			long eraseamask = erasea | (erasea << 1);
			eraseamask |= (eraseamask << 2) | (erasea << 4);
			long erasebmask = eraseb | (eraseb << 1);
			erasebmask |= (erasebmask << 2) | (eraseb << 4);
			long erasecmask = erasec | (erasec << 1);
			erasecmask |= (erasecmask << 2) | (erasec << 4);
			long erasedmask = erased | (erased << 1);
			erasedmask |= (erasedmask << 2) | (erased << 4);
			long eraseemask = erasee | (erasee << 1);
			eraseemask |= (eraseemask << 2) | (erasee << 4);
			long erasefmask = erasef | (erasef << 1);
			erasefmask |= (erasefmask << 2) | (erasef << 4);
			*/
			long tempdata = (a & eraseamask) | (b & erasebmask) | (c & erasecmask) | (d & erasedmask) | (e & eraseemask) | (f & erasefmask);
			tempdata |= tempdata >>> 30;
		    tempdata |= tempdata >>> 15;
			tempdata |= (tempdata >>> 5) | (tempdata >>> 10);
			int erasecolornum = Long.bitCount(tempdata & (long)31);
			field.nrensa++;
			field.score += scoretable[field.nrensa][erasenum][erasecolornum];
			// 消える場所の隣のおじゃまも消していく
			
			long savederasea = erasea;
			long savederaseb = eraseb;
			long savederasec = erasec;
			long savederased = erased;
			long savederasee = erasee;
			long savederasef = erasef;
			
			erasea |= ojamaa & ((savederasea << 5) | (savederasea >>> 5) | savederaseb);
			eraseb |= ojamab & ((savederaseb << 5) | (savederaseb >>> 5) | savederasea | savederasec);
			erasec |= ojamac & ((savederasec << 5) | (savederasec >>> 5) | savederaseb | savederased);
			erased |= ojamad & ((savederased << 5) | (savederased >>> 5) | savederasec | savederasee);
			erasee |= ojamae & ((savederasee << 5) | (savederasee >>> 5) | savederased | savederasef);
			erasef |= ojamaf & ((savederasef << 5) | (savederasef >>> 5) | savederasee);
			
			/*// これは間違い　列aのおじゃまが消えたとき、列bのおじゃまで、消えた列aの隣であるものも消えてしまう
			erasea |= ojamaa & ((erasea << 5) | (erasea >>> 5) | eraseb);
			eraseb |= ojamab & ((eraseb << 5) | (eraseb >>> 5) | erasea | erasec);
			erasec |= ojamac & ((erasec << 5) | (erasec >>> 5) | eraseb | erased);
			erased |= ojamad & ((erased << 5) | (erased >>> 5) | erasec | erasee);
			erasee |= ojamae & ((erasee << 5) | (erasee >>> 5) | erased | erasef);
			erasef |= ojamaf & ((erasef << 5) | (erasef >>> 5) | erasee);
			*/ 
			// ぷよを落とす
			// pextが使えればよかったのだが、やり方が分からない。
			// magic bitboard におけるmagic numberを使うと、ぷよの順番が保たれないのでこれは使えない。
			// 以上よりループを使う。
			
			while (erasea != 0) {
				long temperase = erasea & (-erasea);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = a & moveplace;
				long mvo = ojamaa & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				a = (mv >>> 5) | (a & saveplace);
				ojamaa = (mvo >>> 5) | (ojamaa & saveplace);
				erasea ^= temperase;
				erasea >>>= 5;
			}
			while (eraseb != 0) {
				long temperase = eraseb & (-eraseb);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = b & moveplace;
				long mvo = ojamab & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				b = (mv >>> 5) | (b & saveplace);
				ojamab = (mvo >>> 5) | (ojamab & saveplace);
				eraseb ^= temperase;
				eraseb >>>= 5;
			}
			while (erasec != 0) {
				long temperase = erasec & (-erasec);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = c & moveplace;
				long mvo = ojamac & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				c = (mv >>> 5) | (c & saveplace);
				ojamac = (mvo >>> 5) | (ojamac & saveplace);
				erasec ^= temperase;
				erasec >>>= 5;
			}
			while (erased != 0) {
				long temperase = erased & (-erased);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = d & moveplace;
				long mvo = ojamad & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				d = (mv >>> 5) | (d & saveplace);
				ojamad = (mvo >>> 5) | (ojamad & saveplace);
				erased ^= temperase;
				erased >>>= 5;
			}
			while (erasee != 0) {
				long temperase = erasee & (-erasee);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = e & moveplace;
				long mvo = ojamae & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				e = (mv >>> 5) | (e & saveplace);
				ojamae = (mvo >>> 5) | (ojamae & saveplace);
				erasee ^= temperase;
				erasee >>>= 5;
			}
			while (erasef != 0) {
				long temperase = erasef & (-erasef);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				long mv = f & moveplace;
				long mvo = ojamaf & moveplace;
				mv &= mv - 1;
				mvo &= mvo - 1;
				f = (mv >>> 5) | (f & saveplace);
				ojamaf = (mvo >>> 5) | (ojamaf & saveplace);
				erasef ^= temperase;
				erasef >>>= 5;
			}
			field.field[0] = a;
			field.field[1] = b;
			field.field[2] = c;
			field.field[3] = d;
			field.field[4] = e;
			field.field[5] = f;
			field.ojama[0] = ojamaa;
			field.ojama[1] = ojamab;
			field.ojama[2] = ojamac;
			field.ojama[3] = ojamad;
			field.ojama[4] = ojamae;
			field.ojama[5] = ojamaf;
			return true;
		}
	}
	
	public boolean CalcNextwithoutOjama(FieldInfo field) {
		// まず周囲4つとつながっているかを見る
		long a = field.field[0];
		long b = field.field[1];
		long c = field.field[2];
		long d = field.field[3];
		long e = field.field[4];
		long f = field.field[5];
		
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
		
		// erasea~fをいじる前に得点を計算しておく
		int temperasenum1 = Long.bitCount(erasea | (eraseb << 1) | (erasec << 2));
		int temperasenum2 = Long.bitCount(erased | (erasee << 1) | (erasef << 2));
		int erasenum = temperasenum1 + temperasenum2;
		if (erasenum == 0) {
			return false;
		}
		else {
			// 消える色の数を見る
			long eraseamask = erasea * 31;
			long erasebmask = eraseb * 31;
			long erasecmask = erasec * 31;
			long erasedmask = erased * 31;
			long eraseemask = erasee * 31;
			long erasefmask = erasef * 31;
			
			/*// これは間違い　消える色の上に青色（1番目）のぷよがあると、青色も消える色に含めてしまう
			long eraseamask = erasea | (erasea << 1);
			eraseamask |= (eraseamask << 2) | (erasea << 4);
			long erasebmask = eraseb | (eraseb << 1);
			erasebmask |= (erasebmask << 2) | (eraseb << 4);
			long erasecmask = erasec | (erasec << 1);
			erasecmask |= (erasecmask << 2) | (erasec << 4);
			long erasedmask = erased | (erased << 1);
			erasedmask |= (erasedmask << 2) | (erased << 4);
			long eraseemask = erasee | (erasee << 1);
			eraseemask |= (eraseemask << 2) | (erasee << 4);
			long erasefmask = erasef | (erasef << 1);
			erasefmask |= (erasefmask << 2) | (erasef << 4);
			*/
			long tempdata = (a & eraseamask) | (b & erasebmask) | (c & erasecmask) | (d & erasedmask) | (e & eraseemask) | (f & erasefmask);
			tempdata |= tempdata >>> 30;
		    tempdata |= tempdata >>> 15;
			tempdata |= (tempdata >>> 5) | (tempdata >>> 10);
			int erasecolornum = Long.bitCount(tempdata & (long)31);
			field.nrensa++;
			field.score += scoretable[field.nrensa][erasenum][erasecolornum];
			
			// ぷよを落とす
			// pextが使えればよかったのだが、やり方が分からない。
			// magic bitboard におけるmagic numberを使うと、ぷよの順番が保たれないのでこれは使えない。
			// 以上よりループを使う。
			
			while (erasea != 0) {
				long temperase = erasea & (-erasea); // eraseaの立っているビットのうち一番下のビットを取り出す
				long moveplace = -temperase; // eraseaの立っているビットのうち一番下のビットと、そこより上のビット
				long saveplace = ~moveplace; // eraseaの立っているビットのうち一番下のビットよりも下のビット
				long mv = a & moveplace;
				mv &= mv - 1; // mvの一番下のビットをおろす
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
			field.field[0] = a;
			field.field[1] = b;
			field.field[2] = c;
			field.field[3] = d;
			field.field[4] = e;
			field.field[5] = f;
			return true;
		}
	}
	
	public void ThinkNextActions(FieldInfo field) {
		// 可能手の探索
		long a = field.field[0];
		long b = field.field[1];
		long c = field.field[2];
		long d = field.field[3];
		long e = field.field[4];
		long f = field.field[5];
		long ojamaa = field.ojama[0];
		long ojamab = field.ojama[1];
		long ojamac = field.ojama[2];
		long ojamad = field.ojama[3];
		long ojamae = field.ojama[4];
		long ojamaf = field.ojama[5];
		int atop = TopIndex(a | ojamaa); // この列に何もなかったら0, 1段目にあったら1
		int btop = TopIndex(b | ojamab);
		int ctop = TopIndex(c | ojamac);
		int dtop = TopIndex(d | ojamad);
		int etop = TopIndex(e | ojamae);
		int ftop = TopIndex(f | ojamaf);
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
		int nextfirstpuyo = field.firstpuyo;
		int nextsecondpuyo = field.secondpuyo;
		if (nextfirstpuyo == nextsecondpuyo) {
			field.availableactions = availableactionstable[0][at][bt][ct][dt][et][ft];
			putplaces = availableputplacestable[0][at][bt][ct][dt][et][ft];
		}
		else {
			field.availableactions = availableactionstable[1][at][bt][ct][dt][et][ft];
			putplaces = availableputplacestable[1][at][bt][ct][dt][et][ft];
		}
		long[] tempfield = {a, b, c, d, e, f};
		field.fieldafteravailableactions = new long[field.availableactions.length][6];
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
		for (int i=0;i<field.availableactions.length;i++) {
			for (int j=0;j<6;j++) {
				field.fieldafteravailableactions[i][j] = tempfield[j];
			}
			if (field.availableactions[i][0] == 2) {
				// secondpuyoを先に置くとき
				field.fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextsecondpuyo)) | ((long)1 << (temptops[putplaces[i][0]] + 5 + nextfirstpuyo));
			}
			else if (field.availableactions[i][0] == 0) {
				// firstpuyoを先に置くとき
				field.fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextfirstpuyo)) | ((long)1 << (temptops[putplaces[i][1]] + 5 + nextsecondpuyo));
			}
			else {
				// 横に置くとき
				field.fieldafteravailableactions[i][putplaces[i][0]] |= ((long)1 << (temptops[putplaces[i][0]] + nextfirstpuyo));
				field.fieldafteravailableactions[i][putplaces[i][1]] |= ((long)1 << (temptops[putplaces[i][1]] + nextsecondpuyo));
			}
		}
	}
	
	public void Calc(FieldInfo field) {
		if (!OjamaInField(field)) {
			while(CalcNextwithoutOjama(field));
		}
		else {
			while (CalcNext(field));
		}
	}
	
    public FieldInfo[] ThinkNextActionsAndCalc(FieldInfo field) {
    	// 既に連鎖が計算された後の盤面を入力として、
    	// 次のツモの置き方全通り分について、置いてそれによる連鎖が終わった後の盤面を返す
    	ThinkNextActions(field);
    	FieldInfo[] nextFields = field.AvailableFields();
    	for (int i = 0; i < nextFields.length; i++) {
    		Calc(nextFields[i]);
    		FallDownOjama(nextFields[i]);
    	}
    	return nextFields;
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
	

	public long[] FallDownPuyo(long inputx, long inputy, long eraseplace) {
		if (inputy == 0) {
			// おじゃまがない場合
			while (eraseplace != 0) {
				long temperase = eraseplace & (-eraseplace);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				inputx = inputx >>> 5 & moveplace | (inputx & saveplace);
				eraseplace ^= temperase;
				eraseplace >>>= 5;
			}
		}
		else {
			while (eraseplace != 0) {
				long temperase = eraseplace & (-eraseplace);
				long moveplace = -temperase;
				long saveplace = ~moveplace;
				inputx = inputx >>> 5 & moveplace | (inputx & saveplace);
				inputy = inputy >>> 5 & moveplace | (inputy & saveplace);
				eraseplace ^= temperase;
				eraseplace >>>= 5;
			}
		}
		long[] output = {inputx, inputy};
		return output;
	}
	
	public long FallDownPuyo(long inputx, long eraseplace) {
		// おじゃまがない場合
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
	
	public void ThinkPlacetoFire(FieldInfo field) {
		// 置くと発火する場所をリストアップする
		// 自分・相手それぞれの3つのツモを置いた後の全状態について呼び出したとき、
		// 最初のターンのみ80ms、以降1ターンあたり10ms程度かかる
		
		// 各列の最初のビットを取り出す
		long a = field.field[0];
		long b = field.field[1];
		long c = field.field[2];
		long d = field.field[3];
		long e = field.field[4];
		long f = field.field[5];
		long ojamaa = field.ojama[0];
		long ojamab = field.ojama[1];
		long ojamac = field.ojama[2];
		long ojamad = field.ojama[3];
		long ojamae = field.ojama[4];
		long ojamaf = field.ojama[5];
		long mask = 37191016277640225L;
		long atoprised = TopRised(a | ojamaa);
		long btoprised = TopRised(b | ojamab);
		long ctoprised = TopRised(c | ojamac);
		long dtoprised = TopRised(d | ojamad);
		long etoprised = TopRised(e | ojamae);
		long ftoprised = TopRised(f | ojamaf);
		// 表面の場所を取り出す
		long afilled = atoprised == 0 ? 0 : ((~atoprised) ^ (-atoprised)) & mask;
		long bfilled = btoprised == 0 ? 0 : ((~btoprised) ^ (-btoprised)) & mask;
		long cfilled = ctoprised == 0 ? 0 : ((~ctoprised) ^ (-ctoprised)) & mask;
		long dfilled = dtoprised == 0 ? 0 : ((~dtoprised) ^ (-dtoprised)) & mask;
		long efilled = etoprised == 0 ? 0 : ((~etoprised) ^ (-etoprised)) & mask;
		long ffilled = ftoprised == 0 ? 0 : ((~ftoprised) ^ (-ftoprised)) & mask;
		
		long putamask = ((atoprised * 62) & mask) * 0x3FF; // aの上2段分のビットだけが1になったマスク
		long putbmask = ((btoprised * 62) & mask) * 0x3FF; 
		long putcmask = ((ctoprised * 62) & mask) * 0x3FF; 
		long putdmask = ((dtoprised * 62) & mask) * 0x3FF; 
		long putemask = ((etoprised * 62) & mask) * 0x3FF;
		long putfmask = ((ftoprised * 62) & mask) * 0x3FF; 
		
		long asurface = (afilled & putbmask) | (a > ojamaa ? ((long)1 << (TopIndexb5(a) - 5)) : 0);
		long bsurface = (bfilled & (putamask | putcmask)) | (b > ojamab ? ((long)1 << (TopIndexb5(b) - 5)) : 0);
		long csurface = (cfilled & (putbmask | putdmask)) | (c > ojamac ? ((long)1 << (TopIndexb5(c) - 5)) : 0);
		long dsurface = (dfilled & (putcmask | putemask)) | (d > ojamad ? ((long)1 << (TopIndexb5(d) - 5)) : 0);
		long esurface = (efilled & (putdmask | putfmask)) | (e > ojamae ? ((long)1 << (TopIndexb5(e) - 5)) : 0);
		long fsurface = (ffilled & putemask) | (f > ojamaf ? ((long)1 << (TopIndexb5(f) - 5)) : 0);
		asurface ^= ojamaa;
		bsurface ^= ojamab;
		csurface ^= ojamac;
		dsurface ^= ojamad;
		esurface ^= ojamae;
		fsurface ^= ojamaf;
		
		// 後でつながっているぷよを消すために、どこがつながっているかを見る
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
		
		 // a列の表面にあるぷよで、（他の列にまたがっていてもよいので）2個ぷよがつながっている場所
		// ビット位置で色も表している
		long connect2erasea = 0;
		long connect2eraseb = 0;
		long connect2erasec = 0;
		long connect2erased = 0;
		long connect2erasee = 0;
		long connect2erasef = 0;
		
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
			
			if (connectnum == 2) {
				connect2erasea |= erasea * erasecolor;
				connect2eraseb |= eraseb * erasecolor;
				connect2erasec |= erasec * erasecolor;
			}
			else if (connectnum == 3) {
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
			if (connectnum == 2) {
				connect2erasea |= erasea * erasecolor;
				connect2eraseb |= eraseb * erasecolor;
				connect2erasec |= erasec * erasecolor;
				connect2erased |= erased * erasecolor;
			}
			else if (connectnum == 3) {
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
			if (connectnum == 2) {
				connect2erasea |= erasea * erasecolor;
				connect2eraseb |= eraseb * erasecolor;
				connect2erasec |= erasec * erasecolor;
				connect2erased |= erased * erasecolor;
				connect2erasee |= erasee * erasecolor;
			}
			else if (connectnum == 3) {
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
			if (connectnum == 2) {
				connect2eraseb |= eraseb * erasecolor;
				connect2erasec |= erasec * erasecolor;
				connect2erased |= erased * erasecolor;
				connect2erasee |= erasee * erasecolor;
				connect2erasef |= erasef * erasecolor;
			}
			else if (connectnum == 3) {
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
			if (connectnum == 2) {
				connect2erasec |= erasec * erasecolor;
				connect2erased |= erased * erasecolor;
				connect2erasee |= erasee * erasecolor;
				connect2erasef |= erasef * erasecolor;
			}
			else if (connectnum == 3) {
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
			if (connectnum == 2) {
				connect2erased |= erased * erasecolor;
				connect2erasee |= erasee * erasecolor;
				connect2erasef |= erasef * erasecolor;
			}
			else if (connectnum == 3) {
				connect3erased |= erased * erasecolor;
				connect3erasee |= erasee * erasecolor;
				connect3erasef |= erasef * erasecolor;
			}
		}
		boolean[][] placetofire1= new boolean[5][6]; // 1個で発火できる場所
		boolean[][] placetofire2= new boolean[5][6]; // 2個で発火できる場所

		long putacolor = (((connect2erasea << 5) | connect2eraseb) & putamask) >>> TopIndexb5(a);
		long putbcolor = ((connect2erasea | (connect2eraseb << 5) | connect2erasec) & putbmask) >>> TopIndexb5(b);
		long putccolor = ((connect2eraseb | (connect2erasec << 5) | connect2erased) & putcmask) >>> TopIndexb5(c);
		long putdcolor = ((connect2erasec | (connect2erased << 5) | connect2erasee) & putdmask) >>> TopIndexb5(d);
		long putecolor = ((connect2erased | (connect2erasee << 5) | connect2erasef) & putemask) >>> TopIndexb5(e);
		long putfcolor = ((connect2erasee | (connect2erasef << 5)) & putfmask) >>> TopIndexb5(f);
		
		putacolor |= (putacolor >>> 5);
		putbcolor |= (putbcolor >>> 5);
		putccolor |= (putccolor >>> 5);
		putdcolor |= (putdcolor >>> 5);
		putecolor |= (putecolor >>> 5);
		putfcolor |= (putfcolor >>> 5);
		
		boolean[] putacolorarr = LongBitstoBooleanArray(putacolor, 5);
		boolean[] putbcolorarr = LongBitstoBooleanArray(putbcolor, 5);
		boolean[] putccolorarr = LongBitstoBooleanArray(putccolor, 5);
		boolean[] putdcolorarr = LongBitstoBooleanArray(putdcolor, 5);
		boolean[] putecolorarr = LongBitstoBooleanArray(putecolor, 5);
		boolean[] putfcolorarr = LongBitstoBooleanArray(putfcolor, 5);
		
		for (int i=0;i<5;i++) {
			placetofire2[i][0] = putacolorarr[i];
			placetofire2[i][1] = putbcolorarr[i];
			placetofire2[i][2] = putccolorarr[i];
			placetofire2[i][3] = putdcolorarr[i];
			placetofire2[i][4] = putecolorarr[i];
			placetofire2[i][5] = putfcolorarr[i];
		}

		putacolor = (((connect3erasea << 5) | connect3eraseb) & putamask) >>> TopIndexb5(a);
		putbcolor = ((connect3erasea | (connect3eraseb << 5) | connect3erasec) & putbmask) >>> TopIndexb5(b);
		putccolor = ((connect3eraseb | (connect3erasec << 5) | connect3erased) & putcmask) >>> TopIndexb5(c);
		putdcolor = ((connect3erasec | (connect3erased << 5) | connect3erasee) & putdmask) >>> TopIndexb5(d);
		putecolor = ((connect3erased | (connect3erasee << 5) | connect3erasef) & putemask) >>> TopIndexb5(e);
		putfcolor = ((connect3erasee | (connect3erasef << 5)) & putfmask) >>> TopIndexb5(f);
		
		putacolor |= (putacolor >>> 5);
		putbcolor |= (putbcolor >>> 5);
		putccolor |= (putccolor >>> 5);
		putdcolor |= (putdcolor >>> 5);
		putecolor |= (putecolor >>> 5);
		putfcolor |= (putfcolor >>> 5);
		
		putacolorarr = LongBitstoBooleanArray(putacolor, 5);
		putbcolorarr = LongBitstoBooleanArray(putbcolor, 5);
		putccolorarr = LongBitstoBooleanArray(putccolor, 5);
		putdcolorarr = LongBitstoBooleanArray(putdcolor, 5);
		putecolorarr = LongBitstoBooleanArray(putecolor, 5);
		putfcolorarr = LongBitstoBooleanArray(putfcolor, 5);
		
		for (int i=0;i<5;i++) {
			placetofire1[i][0] = putacolorarr[i];
			placetofire1[i][1] = putbcolorarr[i];
			placetofire1[i][2] = putccolorarr[i];
			placetofire1[i][3] = putdcolorarr[i];
			placetofire1[i][4] = putecolorarr[i];
			placetofire1[i][5] = putfcolorarr[i];
		}
		
		field.placetofire1 = placetofire1;
		field.placetofire2 = placetofire2;
	}
	
	public boolean[] LongBitstoBooleanArray(long x, int bitnum) {
		// xの下位bitnum個のビットをboolean型の配列に変換する
		boolean[] y = new boolean[bitnum];
		for (int i=0;i<bitnum;i++) {
			y[i] = (x & 1) != 0;
			x >>>= 1;
		}
		return y;
	}
	
	public void CalcPlacetoFire(FieldInfo field) {
		while (CalcNext(field)) {
			
		}
		field.field = Arrays.copyOf(field.field, field.field.length);
		field.ojama = Arrays.copyOf(field.ojama, field.ojama.length);
		ThinkPlacetoFire(field);
	}
	
	public void CalcPlacetoFire(FieldInfo field, int ojamanum, int befscore) {
		while (CalcNext(field)) {
			
		}
		int ojamadan = (ojamanum - (befscore + field.score) / 70 + 5) / 6;
		FallDownOjama(field, ojamadan);
		ThinkPlacetoFire(field);
	}
	
	
	public int TsumotoTsumoIndex(int firstpuyo, int secondpuyo) {
		// firstpuyo, secondpuyo は0からはじまる
		// ツモが0オリジンなのはこの関数とActiontoActionIndexのみ
		switch (firstpuyo) {
		case 0:
			return secondpuyo;
		case 1:
			return secondpuyo + 4;
		case 2:
			return secondpuyo + 7;
		case 3:
			return secondpuyo + 9;
		case 4:
			return secondpuyo + 10;
		default:
			return 0;
		}
	}

	public int[] TsumoIndextoTsumo(int tsumoindex){
		// firstpuyo, secondpuyo は1からはじまる
		// TsumoIndextoTsumo(TsumotoTsumoIndex(x, y)) = (x+1, y+1)
		if (tsumoindex < 5) {
			int[] tsumo = new int[] {1, tsumoindex + 1};
			return tsumo;
		}
		else if (tsumoindex < 9) {
			int[] tsumo = new int[] {2, tsumoindex - 3};
			return tsumo;
		}
		else if (tsumoindex < 12) {
			int[] tsumo = new int[] {3, tsumoindex - 6};
			return tsumo;
		}
		else if (tsumoindex < 14) {
			int[] tsumo = new int[] {4, tsumoindex - 8};
			return tsumo;
		}
		else {
			int[] tsumo = new int[] {5, 5};
			return tsumo;
		}	
	}
	
	public int TsumotoTsumoIndex2(int firstpuyo, int secondpuyo) {
		// assert firstpuyo < secondpuyo
		// assert 0 <= firstpuyo < 5
		// assert 0 <= secondpuyo < 5
		// 色の異なる2つのぷよを入力として、
		// それを0~9のインデックスにして返す
		return (-firstpuyo  + 7) * firstpuyo / 2 - 1 + secondpuyo;
	}
	
	public double[] TsumoWeight() {
		// TsumotoTsumoIndex(x, y) の返り値をz、この関数の返す配列をaとしたとき、
		// a[z] = (x, y のツモが出てくる確率)
		double[] weight = new double[15];
		for (int i=0;i<5;i++) {
			weight[TsumotoTsumoIndex(i, i)] = 1.0 / 25; // 同色ツモは異色ツモの半分の確率
			for (int j=i+1;j<5;j++) {
				weight[TsumotoTsumoIndex(i, j)] = 2.0 / 25;
			}
		}
		return weight;
	}
	
	public int[] ActiontoActionIndex(int firstpuyo, int secondpuyo, int firstcolumn, int secondcolumn, boolean isup) {
		// firstpuyoをfirstcolumnに、secondpuyoをsecondcolumnに配置するとき、
		// ツモ(firstpuyo、 secondpuyo の組15通り)を一意に識別する（関数外でも統一してある）インデックスと、
		// ツモと配置の組（275通り）を一意に識別する（この関数の出力からしか出ない）インデックスを返す
		
		if (firstpuyo > secondpuyo) {
			// TsumotoTsumoIndex2(which asserts firstpuyo < secondpuyo)、
			// TsumotoTsumoIndex(which asserts firstpuyo <= secondpuyo)に入れるために、
			// firstpuyoとsecondpuyo、firstcolumnとsecondcolumnをそれぞれ入れ替える
			int temp = firstpuyo;
			firstpuyo = secondpuyo;
			secondpuyo = temp;
			int temp2 = firstcolumn;
			firstcolumn = secondcolumn;
			secondcolumn = temp2;
		}
		
		int tsumoindex = TsumotoTsumoIndex(firstpuyo, secondpuyo);
		int[]output = {tsumoindex, 0};
		if (firstpuyo == secondpuyo) {
			output[1] = 220 + firstpuyo * 11;
			if (firstcolumn == secondcolumn) {
				output[1] += firstcolumn;
			}
			else {
				output[1] += Math.min(firstcolumn, secondcolumn) + 6;
			}
		}
		else {
			output[1] = TsumotoTsumoIndex2(firstpuyo, secondpuyo) * 22;
			if (firstcolumn == secondcolumn) {
				if (isup) {
					output[1] += firstcolumn;
				}
				else {
					output[1] += firstcolumn + 6;
				}
			}
			else {
				if (firstcolumn < secondcolumn) {
					output[1] += firstcolumn + 12;
				}
				else {
					output[1] += secondcolumn + 17;
				}
			}
		}
		return output;
	}
	
	public int[] Tsumos(FieldInfo field){
		// ツモ15通りに対する動き計275通り全てに対してそのターンで取る点数を計算し、
		// ツモ15通りに対して取り得る最大点数を長さ15の配列で返す
		boolean[] alreadythought = new boolean[275];
		int[] maxscores = new int[15];
		int[] tops = new int[6];
		for (int i=0;i<6;i++) {
			tops[i] = TopIndexb5(field.field[i] | field.ojama[i]); 
		}
		// 13段目に（黄色以外の）ぷよを置いてしまうことがあるのでこれを修正する
		// 13段目の処理は別にあとで考える
		// いまはまだ13段目に置くことで高い点数を出せることが分かったとしても
		// 実際に置く時になってその手を選べない
		for (int i=0;i<5;i++) {
			// i ... ぷよの色
			for (int j=0;j<6;j++) {
				// j ... 列
				if (field.placetofire2[i][j]) {
					if (j > 0) {
						int[] actionindex = ActiontoActionIndex(i, i, j, j-1, false);
						if (!alreadythought[actionindex[1]]) {
							if ((tops[j] < 60) & (tops[j - 1] < 60)) {
								 FieldInfo tempf = new FieldInfo(field, i, i, j, j-1, tops, false);
								 Calc(tempf);
								 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
							}
							 alreadythought[actionindex[1]] = true;
						}
					}
					if (j == 0) {
						// field.placetofire2[i][0]が true のときのみ上の分岐では見れないのでこちらに
						// あとでもっと分かりやすく直す
						int[] actionindex = ActiontoActionIndex(i, i, j, j+1, false);
						if (!alreadythought[actionindex[1]]) {
							if ((tops[j] < 60) & (tops[j + 1] < 60)) {
								 FieldInfo tempf = new FieldInfo(field, i, i, j, j+1, tops, false);
								 Calc(tempf);
								 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
							}
							 alreadythought[actionindex[1]] = true;
						}
					}
					int[] actionindex = ActiontoActionIndex(i, i, j, j, false);
					if (!alreadythought[actionindex[1]]) {
						if (tops[j] < 55) {
							 FieldInfo tempf = new FieldInfo(field, i, i, j, j, tops, false);
							 Calc(tempf);
							 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
						}
						 alreadythought[actionindex[1]] = true;
					}
					/*
					actionindex = ActiontoActionIndex(i, i, j, j, true);
					if (!alreadythought[actionindex[1]]) {
						 FieldInfo tempf = new FieldInfo(field, i, i, j, j, tops, true);
						 Calc(tempf);
						 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
						 alreadythought[actionindex[1]] = true;
					}
					*/
				}
				if (field.placetofire1[i][j]) {
					for (int k=0;k<5;k++) {
						if (j > 0) {
							int[] actionindex = ActiontoActionIndex(i, k, j, j-1, false);
							if (!alreadythought[actionindex[1]]) {
								if ((tops[j] < 60) & (tops[j - 1] < 60)) {
									 FieldInfo tempf = new FieldInfo(field, i, k, j, j-1, tops, false);
									 Calc(tempf);
									 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
								}
								 alreadythought[actionindex[1]] = true;
							}
						}
						if (j < 5) {
							int[] actionindex = ActiontoActionIndex(i, k, j, j+1, false);
							if (!alreadythought[actionindex[1]]) {
								if ((tops[j] < 60) & (tops[j + 1] < 60)) {
									 FieldInfo tempf = new FieldInfo(field, i, k, j, j+1, tops, false);
									 Calc(tempf);
									 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
								}
								 alreadythought[actionindex[1]] = true;
							}
						}
						int[] actionindex = ActiontoActionIndex(i, k, j, j, false);
						if (!alreadythought[actionindex[1]]) {
							if (tops[j] < 55) {
								 FieldInfo tempf = new FieldInfo(field, i, k, j, j, tops, false);
								 Calc(tempf);
								 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
							}
							 alreadythought[actionindex[1]] = true;
						}
						actionindex = ActiontoActionIndex(i, k, j, j, true);
						if (!alreadythought[actionindex[1]]) {
							if (tops[j] < 55) {
								 FieldInfo tempf = new FieldInfo(field, i, k, j, j, tops, true);
								 Calc(tempf);
								 maxscores[actionindex[0]] = Math.max(maxscores[actionindex[0]], tempf.score);
							}
							 alreadythought[actionindex[1]] = true;
						}
					}
				}
			}
		}
		return maxscores;
	}
	
	public void CompareScores(int[] maxscoreforeachtsumo, int[] maxscoresofthismove, int scoreofthismove) {
		// 入力A, B, c に対して、
		// D = B + c
		// A[A < D] = D[A < D]
		for (int i=0;i<15;i++) {
			maxscoreforeachtsumo[i] = Math.max(maxscoreforeachtsumo[i], maxscoresofthismove[i] + scoreofthismove);
		}
	}
	
	public void FallDownOjama(FieldInfo field, int ojamadan) {
		// 元のmeow用の関数
		long mask = 0x0084210842108421L;
		long ojama = mask >>> (60 - ojamadan * 5);
		for (int i=0;i<6;i++) {
			long filled = TopIndex((field.field[i] | field.ojama[i])) * 5;
			field.ojama[i] |= ojama << filled;
			field.ojama[i] &= mask;
		}
		field.field = Arrays.copyOf(field.field, 6);
		field.ojama = Arrays.copyOf(field.ojama, 6);
	}
	
	public void FallDownOjama(FieldInfo field) {
		// meowBeam用の関数
		
		// 直前の発火点数による相殺を行う
		field.scoreCarry += field.score;
		int ojamadan = Math.max(0, (field.ojamaList[0] - field.score / 70 + 5) / 6);
		int j = 0;
		while (field.scoreCarry != 0 && (j < field.ojamaList.length)) {
			if (field.scoreCarry < field.ojamaList[j] * 70) {
				field.ojamaList[j] -= field.scoreCarry / 70;
				field.scoreCarry = 0;
			}
			else {
				field.scoreCarry -= field.ojamaList[j] * 70;
				field.ojamaList[j] = 0;
			}
			j++;
		}
		if (ojamadan > 0) {
			// おじゃまを降らす
			long mask = 0x0084210842108421L;
			long ojama = mask >>> (60 - ojamadan * 5);
			for (int i=0;i<6;i++) {
				long filled = TopIndex((field.field[i] | field.ojama[i])) * 5;
				field.ojama[i] |= ojama << filled;
				field.ojama[i] &= mask;
			}
			field.field = Arrays.copyOf(field.field, 6);
			field.ojama = Arrays.copyOf(field.ojama, 6);
		}
	}
	
	public double EvaluateFieldFast(FieldInfo field) {
		// まず同じ色のぷよがつながっている辺の数を計算する
		long a = field.field[0];
		long b = field.field[1];
		long c = field.field[2];
		long d = field.field[3];
		long e = field.field[4];
		long f = field.field[5];
		long ojamaa = field.ojama[0];
		long ojamab = field.ojama[1];
		long ojamac = field.ojama[2];
		long ojamad = field.ojama[3];
		long ojamae = field.ojama[4];
		long ojamaf = field.ojama[5];

		if ((ojamaa | ojamab | ojamac | ojamad | ojamae | ojamaf) == 0){
			// 盤面におじゃまがない場合
			return bnfno.EvaluateFieldFast(field.field);
		}
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
		long atoprised = TopRised(a | ojamaa);
		long btoprised = TopRised(b | ojamab);
		long ctoprised = TopRised(c | ojamac);
		long dtoprised = TopRised(d | ojamad);
		long etoprised = TopRised(e | ojamae);
		long ftoprised = TopRised(f | ojamaf);
		// 表面の場所を取り出す
		long afilled = atoprised == 0 ? 0 : ((~atoprised) ^ (-atoprised)) & mask;
		long bfilled = btoprised == 0 ? 0 : ((~btoprised) ^ (-btoprised)) & mask;
		long cfilled = ctoprised == 0 ? 0 : ((~ctoprised) ^ (-ctoprised)) & mask;
		long dfilled = dtoprised == 0 ? 0 : ((~dtoprised) ^ (-dtoprised)) & mask;
		long efilled = etoprised == 0 ? 0 : ((~etoprised) ^ (-etoprised)) & mask;
		long ffilled = ftoprised == 0 ? 0 : ((~ftoprised) ^ (-ftoprised)) & mask;
		long asurface = (afilled & (~bfilled)) | (a > ojamaa ? (0x1L << (TopIndexb5(a) - 5)) : 0);
		long bsurface = (bfilled & (~(afilled & cfilled))) | (b > ojamab ? (0x1L << (TopIndexb5(b) - 5)) : 0);
		long csurface = (cfilled & (~(bfilled & dfilled))) | (c > ojamac ? (0x1L << (TopIndexb5(c) - 5)) : 0);
		long dsurface = (dfilled & (~(cfilled & efilled))) | (d > ojamad ? (0x1L << (TopIndexb5(d) - 5)) : 0);
		long esurface = (efilled & (~(dfilled & ffilled))) | (e > ojamae ? (0x1L << (TopIndexb5(e) - 5)) : 0);
		long fsurface = (ffilled & (~efilled)) | (f > ojamaf ? (0x1L << (TopIndexb5(f) - 5)) : 0);

		// あと1つのぷよが来れば発火できるものを考えたいので、おじゃまが消えるところから始まる連鎖は考えない
		asurface ^= ojamaa;
		bsurface ^= ojamab;
		csurface ^= ojamac;
		dsurface ^= ojamad;
		esurface ^= ojamae;
		fsurface ^= ojamaf;
		
		// 各表面のぷよについてそれぞれ消してみる
		// その前にいったん今の盤面を保存しておく
		long[] savedfield = {a, b, c, d, e, f};
		long[] savedojama = {ojamaa, ojamab, ojamac, ojamad, ojamae, ojamaf};
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
			// 後1つのぷよで発火できるなら
			// おじゃまを消す
			long eraseojamaa = savedojama[0] & ((erasea << 5) | (erasea >>> 5) | eraseb);
			long eraseojamab = savedojama[1] & ((eraseb << 5) | (eraseb >>> 5) | erasea | erasec);
			long eraseojamac = savedojama[2] & ((erasec << 5) | (erasec >>> 5) | eraseb);
			erasea |= eraseojamaa;
			eraseb |= eraseojamab;
			erasec |= eraseojamac;

			// ぷよを落とす
			long[] as = FallDownPuyo(savedfield[0], savedojama[0], erasea);
			long[] bs = FallDownPuyo(savedfield[1], savedojama[1], eraseb);
			long[] cs = FallDownPuyo(savedfield[2], savedojama[2], erasec);
			long[] ds = {savedfield[3], savedojama[3]};
			long[] es = {savedfield[4], savedojama[4]};
			long[] fs = {savedfield[5], savedojama[5]};
			// 連鎖を計算する
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
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
			long eraseojamaa = savedojama[0] & ((erasea << 5) | (erasea >>> 5) | eraseb);
			long eraseojamab = savedojama[1] & ((eraseb << 5) | (eraseb >>> 5) | erasea | erasec);
			long eraseojamac = savedojama[2] & ((erasec << 5) | (erasec >>> 5) | eraseb | erased);
			long eraseojamad = savedojama[3] & ((erased << 5) | (erased >>> 5) | erasec);
			erasea |= eraseojamaa;
			eraseb |= eraseojamab;
			erasec |= eraseojamac;
			erased |= eraseojamad;
			long[] as = FallDownPuyo(savedfield[0], savedojama[0], erasea);
			long[] bs = FallDownPuyo(savedfield[1], savedojama[1], eraseb);
			long[] cs = FallDownPuyo(savedfield[2], savedojama[2], erasec);
			long[] ds = FallDownPuyo(savedfield[3], savedojama[3], erased);
			long[] es = {savedfield[4], savedojama[4]};
			long[] fs = {savedfield[5], savedojama[5]};
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
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
			long eraseojamaa = savedojama[0] & ((erasea << 5) | (erasea >>> 5) | eraseb);
			long eraseojamab = savedojama[1] & ((eraseb << 5) | (eraseb >>> 5) | erasea | erasec);
			long eraseojamac = savedojama[2] & ((erasec << 5) | (erasec >>> 5) | eraseb | erased);
			long eraseojamad = savedojama[3] & ((erased << 5) | (erased >>> 5) | erasec | erasee);
			long eraseojamae= savedojama[4] & ((erasee << 5) | (erasee >>> 5) | erased);
			erasea |= eraseojamaa;
			eraseb |= eraseojamab;
			erasec |= eraseojamac;
			erased |= eraseojamad;
			erasee |= eraseojamae;
			long[] as = FallDownPuyo(savedfield[0], savedojama[0], erasea);
			long[] bs = FallDownPuyo(savedfield[1], savedojama[1], eraseb);
			long[] cs = FallDownPuyo(savedfield[2], savedojama[2], erasec);
			long[] ds = FallDownPuyo(savedfield[3], savedojama[3], erased);
			long[] es = FallDownPuyo(savedfield[4], savedojama[4], erasee);
			long[] fs = {savedfield[5], savedojama[5]};
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
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
			long eraseojamab = savedojama[1] & ((eraseb << 5) | (eraseb >>> 5) | erasec);
			long eraseojamac = savedojama[2] & ((erasec << 5) | (erasec >>> 5) | eraseb | erased);
			long eraseojamad = savedojama[3] & ((erased << 5) | (erased >>> 5) | erasec | erasee);
			long eraseojamae = savedojama[4] & ((erasee << 5) | (erasee >>> 5) | erased | erasef);
			long eraseojamaf = savedojama[5] & ((erasef << 5) | (erasef >>> 5) | erasee);
			eraseb |= eraseojamab;
			erasec |= eraseojamac;
			erased |= eraseojamad;
			erasee |= eraseojamae;
			erasef |= eraseojamaf;
			long[] as = {savedfield[0], savedojama[0]};
			long[] bs = FallDownPuyo(savedfield[1], savedojama[1], eraseb);
			long[] cs = FallDownPuyo(savedfield[2], savedojama[2], erasec);
			long[] ds = FallDownPuyo(savedfield[3], savedojama[3], erased);
			long[] es = FallDownPuyo(savedfield[4], savedojama[4], erasee);
			long[] fs = FallDownPuyo(savedfield[5], savedojama[5], erasef);
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
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
			long eraseojamac = savedojama[2] & ((erasec << 5) | (erasec >>> 5) | erased);
			long eraseojamad = savedojama[3] & ((erased << 5) | (erased >>> 5) | erasec | erasee);
			long eraseojamae = savedojama[4] & ((erasee << 5) | (erasee >>> 5) | erased | erasef);
			long eraseojamaf = savedojama[5] & ((erasef << 5) | (erasef >>> 5) | erasee);
			erasec |= eraseojamac;
			erased |= eraseojamad;
			erasee |= eraseojamae;
			erasef |= eraseojamaf;
			long[] as = {savedfield[0], savedojama[0]};
			long[] bs = {savedfield[1], savedojama[1]};
			long[] cs = FallDownPuyo(savedfield[2], savedojama[2], erasec);
			long[] ds = FallDownPuyo(savedfield[3], savedojama[3], erased);
			long[] es = FallDownPuyo(savedfield[4], savedojama[4], erasee);
			long[] fs = FallDownPuyo(savedfield[5], savedojama[5], erasef);
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
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
			long eraseojamad = savedojama[3] & ((erased << 5) | (erased >>> 5) | erasee);
			long eraseojamae = savedojama[4] & ((erasee << 5) | (erasee >>> 5) | erased | erasef);
			long eraseojamaf = savedojama[5] & ((erasef << 5) | (erasef >>> 5) | erasee);
			erased |= eraseojamad;
			erasee |= eraseojamae;
			erasef |= eraseojamaf;
			long[] as = {savedfield[0], savedojama[0]};
			long[] bs = {savedfield[1], savedojama[1]};
			long[] cs = {savedfield[2], savedojama[2]};
			long[] ds = FallDownPuyo(savedfield[3], savedojama[3], erased);
			long[] es = FallDownPuyo(savedfield[4], savedojama[4], erasee);
			long[] fs = FallDownPuyo(savedfield[5], savedojama[5], erasef);
			FieldInfo tempfi = new FieldInfo(as, bs, cs, ds, es, fs);
			while (CalcNext(tempfi));
			rensaevaluation = rensaevaluation > tempfi.nrensa ? rensaevaluation : tempfi.nrensa;
		}
		double evaluation = rensaevaluation + connectnum * 0.2;
		return evaluation;
	}
	
	public int OjamaDiscount(FieldInfo field) {
		int output = 0;
		for (int i=0;i<6;i++) {
			output += Long.bitCount(field.ojama[i]);
		}
		return output * 30;
	}
	
	public boolean OjamaInField(FieldInfo field) {
		// 盤面におじゃまがある場合、trueを返す
		return (field.ojama[0] | field.ojama[1] | field.ojama[2] | field.ojama[3] | field.ojama[4] | field.ojama[5]) != 0;
	}
	
	public double ChokudaiSearch(FieldInfo inputfield, int[][] tsumo, int searchwidth) {
		if (!OjamaInField(inputfield)) {
			// おじゃまが盤面にない場合、それを前提とした高速な関数に渡す
			return bnfno.ChokudaiSearch(inputfield, tsumo, searchwidth);
		}
		int searchdepth = tsumo.length;
		FieldInfoQueue[] queues = new FieldInfoQueue[searchdepth + 1];
		for (int i=0;i<searchdepth + 1;i++) {
			queues[i] = new FieldInfoQueue(this);
		}
		queues[0].add(inputfield);
		double evaluation = -10;// ツモと最初のターンの動きに対する評価値
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
				FieldInfo field = queues[i].poll(); 
				if (i != searchdepth - 1) {
					field.nextfirstpuyo = tsumo[i + 1][0];
					field.nextsecondpuyo = tsumo[i + 1][1];
				}
				evaluation = evaluation < field.evaluation ? field.evaluation : evaluation;
				// ツモの動かし方最大22通りにfieldを分岐させる
				FieldInfo[] nextfields = ThinkNextActionsAndCalc(field);
				
				for (FieldInfo nextfield: nextfields) {
					// 評価値を計算してキューに追加
					// 一度見たものはFieldInfoQueue内部のmHashに記録されていて、add()を使っても追加されない
					queues[i + 1].add(nextfield);
				}
			}
			FieldInfo lastfield = queues[searchdepth - 1].poll();
			if (lastfield != null && lastfield.evaluation > evaluation) {
				evaluation = lastfield.evaluation;
			}
		}
		return evaluation;
	}
}
