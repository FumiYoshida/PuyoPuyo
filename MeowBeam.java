package meow2021;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.AbstractPlayer;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Action;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Board;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Field;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo.PuyoDirection;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.storage.PuyoType;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.PuyoPuyo;
import sp.AbstractSamplePlayer;

public class MeowBeam extends AbstractSamplePlayer {
	FastBitNextField bnf;
	Np np;
	
	private double EvaluateAction(FastBitNextField bnf, FieldInfo secondfield, FieldInfo thirdfield) {
		// 2ターン目・3ターン目の行動を評価する
		// 2ターン目・3ターン目で取る点数に、3ターン目が終わった後の盤面の評価を加えたものを返す
		int temp = secondfield.score + thirdfield.score; // 2ターン目・3ターン目で取る点数
		temp +=  2000 - bnf.OjamaDiscount(thirdfield);// 3ターン目終了時に隣接ぷよを消すことでおじゃまを消せていたら1個につき30点評価プラス
		temp += thirdfield.firepossibility; // 3ターン目終了時に、後x個特定の色のぷよが来ればy点出せるとしたとき、x = 1 のときy / 2点、x = 2 または 3 のときy / 4 点加算 
		return (int)temp;
	}
	
	@Override
	public Action doMyTurn() {
		// Maou2020 と436回試合して247:189で負ける(勝率43.3%)  細かいところを直して34:37で勝った　合わせて 281:226
		// さらに盤面評価を高速にしてMaou2020に 277:246で負ける (勝率47.0%) p=0.43なのでまだ有意ではない
		long start = System.currentTimeMillis();
		Board board = getMyBoard();
		if (bnf == null) bnf = new FastBitNextField();
		if (np == null) np = new Np();
		int myactionnum = 0;
		int ojamathreshold = 4;
		
		// おじゃまが降るのは何ターン後か見る
		// grace = 1 ... このターン終了時におじゃまが降る
		// grace = 2 ... 次のターン終了時におじゃまが降る
		// grace = 3 ... 次の次のターン終了時におじゃまが降る
		List<Integer> ojal = board.getNumbersOfOjamaList();
		int grace = 1;
		for (int oja : ojal) {
			if (oja > 0) {
				break;
			}
			grace++;
		}
		FieldInfo myfirstfield = bnf.ReadField(board);
		
		FieldInfo[] myfirstfields = bnf.ThinkNextActionsAndCalc(myfirstfield);
		myactionnum = myfirstfields.length;
		if (myactionnum == 0) {
			System.out.println("参りました");
			return new Action(PuyoDirection.DOWN, 0);
		}
		int[][] savedactions = myfirstfield.availableactions;
		if (grace < 4 && board.getTotalNumberOfOjama() >= ojamathreshold) {
			/* 3ターン以内（最長でもこのターンの次の次のターンが終わったとき）
			 *  におじゃまが降ってくるとき、おじゃまが降る前に連鎖を発火させる。
			 *  発火できないときはカウンター形を築く。
			 */
			/*
			int[] mymaxsumscores = new int[myactionnum];
			for (int i=0;i<myactionnum;i++) {
				FieldInfo[] secondfields = bnf.ThinkNextActionsAndCalc(myfirstfields[i]);
				int[] maxscoresofeachtsumo = new int[15];
				for (int j=0;j<secondfields.length;j++) {
					FieldInfo[] thirdfields = bnf.ThinkNextActionsAndCalc(secondfields[j]);
					for (int k=0;k<thirdfields.length;k++) {
						bnf.CalcPlacetoFire(thirdfields[k], thirdfields[k].ojamaList[0], secondfields[j].scoreCarry);
						int[] fourthscores = bnf.Tsumos(thirdfields[k]);
						bnf.CompareScores(maxscoresofeachtsumo, fourthscores, (int)EvaluateAction(bnf, secondfields[j], thirdfields[k]));
					}
				}
				mymaxsumscores[i] = myfirstfields[i].score + (int)np.dot(maxscoresofeachtsumo, bnf.tsumoWeight);
			}
			int selectindex = np.maxindex(mymaxsumscores);
			*/
			int[] mymaxsumscores = new int[myactionnum];
			for (int i=0;i<myactionnum;i++) {
				FieldInfo[] secondfields = bnf.ThinkNextActionsAndCalc(myfirstfields[i]);
				int[] maxscoresofeachtsumo = new int[15];
				for (int j=0;j<secondfields.length;j++) {
					FieldInfo[] thirdfields = bnf.ThinkNextActionsAndCalc(secondfields[j]);
					for (int k=0;k<thirdfields.length;k++) {
						double temp =  myfirstfields[i].score + secondfields[j].score +  thirdfields[k].score + 6000 - bnf.OjamaDiscount(thirdfields[k]);
						mymaxsumscores[i] = (int)Math.max(mymaxsumscores[i], temp);
						/*
						bnf.CalcPlacetoFire(thirdfields[k], thirdfields[k].ojamaList[0], secondfields[j].scoreCarry);
						int[] fourthscores = bnf.Tsumos(thirdfields[k]);
						bnf.CompareScores(maxscoresofeachtsumo, fourthscores, secondfields[j].score +  thirdfields[k].score + 6000 - bnf.OjamaDiscount(thirdfields[k]));
						*/
					}
				}
				// mymaxsumscores[i] = myfirstfields[i].score + (int)np.dot(maxscoresofeachtsumo, bnf.tsumoWeight);
			}
			int selectindex = np.maxindex(mymaxsumscores);
			PuyoDirection selectdirection = PuyoDirection.values()[savedactions[selectindex][0]];
			int selectcolumn = savedactions[selectindex][1];
			return new Action(selectdirection, selectcolumn);
		}
		else {
			/* まだしばらくは猶予があるとき
			 *  3ターン以内（ネクネクまで使ったとき）に、もしくは
			 *  望ましい色が1つでも来れば4ターン以内発火させることが可能な手
			 * （つまりは発火点をつぶさない手）の内、その発火する連鎖数が最も大きい手を選ぶ。
			 */
			// 相手が4ターン以内に発火できる連鎖を調べる
			FieldInfo enemyfirstfield = bnf.ReadField(getEnemyBoard());
			FieldInfo[] enemyfirstfields = bnf.ThinkNextActionsAndCalc(enemyfirstfield);
			int enemyactionnum = enemyfirstfields.length;
			int enemyfirstmaxscore = 0;
			int enemysecondmaxscore = 0;
			double[][] enemyScoresofEachTsumoEachAction = new double[enemyactionnum][15]; 
			for (int i=0;i<enemyactionnum;i++) {
				FieldInfo[] enemysecondfields = bnf.ThinkNextActionsAndCalc(enemyfirstfields[i]);
				enemyfirstmaxscore = Math.max(enemyfirstmaxscore, enemyfirstfields[i].score);
				int[] enemymaxscoresofeachtsumo = new int[15]; 
				for (int j=0;j<enemysecondfields.length;j++) {
					FieldInfo[] enemythirdfields = bnf.ThinkNextActionsAndCalc(enemysecondfields[j]);
					enemysecondmaxscore = Math.max(enemysecondmaxscore, enemyfirstfields[i].score + enemysecondfields[j].score);
					for (int k=0;k<enemythirdfields.length;k++) {
						bnf.CalcPlacetoFire(enemythirdfields[k]);
						int[] fourthscores = bnf.Tsumos(enemythirdfields[k]);
						int temp = enemysecondfields[j].score + enemythirdfields[k].score;
						bnf.CompareScores(enemymaxscoresofeachtsumo, fourthscores, temp);
					}
				}
				// 現時点では4手目のツモは分からないので、
				// 相手の1手目を仮定する→4手目のツモを仮定する→相手の2手目以降の最善手を考える
				// →ツモについて平均する→相手の1手目のスコアを足す→相手の1手目の最善手を考える
				// という順番でないといけない。
				// 最初から1手目～4手目のスコアの合計を取り、それをツモについて平均すると、
				// 相手が4手目のツモを知った状態で最善手を選ぶことになってしまう。
				// →相手の運を良い方向に見積もりすぎて発火機会を失ってしまう。
				enemyScoresofEachTsumoEachAction[i] = np.add((double)enemyfirstfields[i].score, enemymaxscoresofeachtsumo);
			}
			
			// 4手先まで読む
			int[] firstscores = new int[myactionnum];
			int[] secondscores = new int[myactionnum];
			
			double[][] myScoresofEachTsumoEachAction = new double[myactionnum][15]; 
			for (int i=0;i<myactionnum;i++) {
				FieldInfo[] mysecondfields = bnf.ThinkNextActionsAndCalc(myfirstfields[i]);
				firstscores[i] =  myfirstfields[i].score;
				int[] maxscoresofeachtsumo = new int[15];
				for (int j=0;j<mysecondfields.length;j++) {
					FieldInfo[] mythirdfields = bnf.ThinkNextActionsAndCalc(mysecondfields[j]);
					secondscores[i] = Math.max(secondscores[i], myfirstfields[i].score + mysecondfields[j].score);
					for (int k=0;k<mythirdfields.length;k++) {
						bnf.CalcPlacetoFire(mythirdfields[k]);
						int[] fourthscores = bnf.Tsumos(mythirdfields[k]);		
						bnf.CompareScores(maxscoresofeachtsumo, fourthscores, mysecondfields[j].score + mythirdfields[k].score);
					}
				}
				myScoresofEachTsumoEachAction[i] = np.add((double)myfirstfields[i].score, maxscoresofeachtsumo);
			}
			int firstmaxscore = np.max(firstscores);
			int firstmaxindex = np.maxindex(firstscores);
			int secondmaxscore = np.max(secondscores);
			int secondmaxindex = np.maxindex(secondscores);
			
			// 最低発火点数
			int scorethreshold = 2000;// 1000 
			
			int myscorethreshold = firstmaxscore < scorethreshold ? scorethreshold : 1; 
			
			 // 自分の初手の最大スコア - scorethresholdを、相手がおじゃまが降るまでに上回ることができる確率
			double enemycounterchance = 0;

			 // 自分の次のターンまでの最大スコア - scorethresholdを、相手がおじゃまが降る1ターン前までに上回ることができる確率
			double enemycounterchance2 = 0;
			
			double realfirstmaxscore = firstmaxscore - myscorethreshold - board.getTotalNumberOfOjama() * 70;
			double realsecondmaxscore = secondmaxscore - myscorethreshold - board.getTotalNumberOfOjama() * 70;
			
			
			double enemyojamascore =  getEnemyBoard().getTotalNumberOfOjama() * 70;
			for (int i=0;i<enemyactionnum;i++) {
				double[] tempscores = np.ReLU(np.sub(enemyScoresofEachTsumoEachAction[i], enemyojamascore));
				
				// 相手がこのターンで手iを選んだ時、
				// 自分がこのターンで発火できる最大点数-scorethreshold をおじゃまが降るまでに上回ることが出来る確率
				double tempchance = np.sum(np.slice(bnf.tsumoWeight, np.dainari(tempscores, realfirstmaxscore))); 
				
				enemycounterchance = Math.max(enemycounterchance, tempchance);
				
				// 相手がこのターンで手iを選んだ時、
				// 自分が次のターンまでで発火できる最大点数-scorethreshold をおじゃまが降る1ターン前までに上回ることが出来る確率
				double tempchance2 = np.sum(np.slice(bnf.tsumoWeight, np.dainari(tempscores, realsecondmaxscore))); 
				enemycounterchance2 = Math.max(enemycounterchance2, tempchance2);
			}
			
			 // 相手の初手の最大スコア - scorethresholdを、自分がおじゃまが降るまでに上回ることができる確率
			double mycounterchance = 0;
			double[] mycounterchances = new double[myactionnum];

			 // 相手の次のターンまでの最大スコア - scorethresholdを、自分がおじゃまが降る1ターン前までに上回ることができる確率
			double mycounterchance2 = 0;
			double[] mycounterchances2 = new double[myactionnum];
			
			int enemyscorethreshold = enemyfirstmaxscore < scorethreshold ? scorethreshold : 1; 
			
			double enemyrealfirstmaxscore = enemyfirstmaxscore - enemyscorethreshold - getEnemyBoard().getTotalNumberOfOjama() * 70;
			double enemyrealsecondmaxscore = enemysecondmaxscore - enemyscorethreshold - getEnemyBoard().getTotalNumberOfOjama() * 70;
			
			double myojamascore =  board.getTotalNumberOfOjama() * 70;
			
			int defenceactionindex = 0;
			int defenceactionindex2 = 0;
			for (int i=0;i<myactionnum;i++) {
				double[] tempscores = np.ReLU(np.sub(myScoresofEachTsumoEachAction[i], myojamascore));
				
				// 自分がこのターンで手iを選んだ時、
				// 相手がこのターンで発火できる最大点数-scorethreshold をおじゃまが降るまでに上回ることが出来る確率
				double tempchance = np.sum(np.slice(bnf.tsumoWeight, np.dainari(tempscores, enemyrealfirstmaxscore))); 
				mycounterchances[i] = tempchance;
				if (tempchance > mycounterchance) {
					mycounterchance = tempchance;
					defenceactionindex = i;
				}
				// 自分がこのターンで手iを選んだ時、
				// 相手が次のターンまでで発火できる最大点数-scorethreshold をおじゃまが降る1ターン前までに上回ることが出来る確率
				double tempchance2 = np.sum(np.slice(bnf.tsumoWeight, np.dainari(tempscores, enemyrealsecondmaxscore))); 			
				mycounterchances2[i] = tempchance2;
				if (tempchance2 > mycounterchance2) {
					mycounterchance2 = tempchance2;
					defenceactionindex2 = i;
				}
			}
			double firechancethreshold = CountBlank() < 30 ? 0.5 : 0.2;
			// double puyorate = (double)(72 - CountBlank()) / 72;
			// double firechancethreshold = Math.min(0.1 + Math.pow(puyorate, 2) * 1.6, 0.5);
			System.out.printf("chancethreshold : %s %%\n", (int)(firechancethreshold * 100));
			double defencechancethreshold = 0.25;
			PuyoDirection[] temp = PuyoDirection.values();
			// if ((firstmaxscore - board.getTotalNumberOfOjama() * 70) > Math.max(0, enemymaxscore - getEnemyBoard().getTotalNumberOfOjama() * 70)+ scorethreshold) {
			if (enemycounterchance < firechancethreshold) {
			// 相手の組んでいる連鎖が小さくてこのターンの発火で倒せそうだったら
				System.out.printf("発火します　相手の反撃確率 : %s %%\n", (int)(100 * enemycounterchance));
				PuyoDirection selectdirection = PuyoDirection.values()[savedactions[firstmaxindex][0]];
				int selectcolumn = savedactions[firstmaxindex][1];
				return  new Action(selectdirection, selectcolumn);
			}
			//else if ((secondmaxscore - board.getTotalNumberOfOjama() * 70) > Math.max(0, enemymaxscore - getEnemyBoard().getTotalNumberOfOjama() * 70)+ scorethreshold + 1000) {
			else if (enemycounterchance2 < firechancethreshold) {
				// 次に発火をする条件を満たせる可能性が高かったら
				System.out.println("発火のための準備をします");
				PuyoDirection selectdirection = PuyoDirection.values()[savedactions[secondmaxindex][0]];
				int selectcolumn = savedactions[secondmaxindex][1];
				return  new Action(selectdirection, selectcolumn);
			}
			else if (mycounterchance < defencechancethreshold) {
				if (mycounterchance != 0) {
					// 不利な時
					// 防御策を実施
					System.out.println("相手の発火を危惧して安全策を取りました");
					System.out.printf("相手がこのターンに発火した場合の反撃確率 : %s %%\n", (int)(100 * mycounterchance));

					PuyoDirection selectdirection = PuyoDirection.values()[savedactions[defenceactionindex][0]];
					int selectcolumn = savedactions[defenceactionindex][1];
					return  new Action(selectdirection, selectcolumn);
				}
				else {
					// どうしようもない場合
					// 形作り
					System.out.println("今発火されたらきついです...");
					System.out.printf("相手がこのターンに発火した場合の反撃確率 : %s %%\n", (int)(100 * mycounterchance));
				}
			}
			else if (mycounterchance2 < defencechancethreshold) {
				if (mycounterchance2 != 0) {
					// 不利な時
					// 防御策を実施
					System.out.println("相手の次のターンの発火を危惧して安全策を取りました");
					System.out.printf("相手が次のターンに発火した場合の反撃確率 : %s %%\n", (int)(100 * mycounterchance2));
					PuyoDirection selectdirection = PuyoDirection.values()[savedactions[defenceactionindex2][0]];
					int selectcolumn = savedactions[defenceactionindex2][1];
					return  new Action(selectdirection, selectcolumn);
				}
				else {
					// どうしようもない場合
					// 形作り
					System.out.println("次のターンに発火されたらきついです...");
					System.out.printf("相手が次のターンに発火した場合の反撃確率 : %s %%\n", (int)(100 * mycounterchance2));
				}
			}
			else {
				if (enemycounterchance != 1) System.out.printf("自分がこのターンで発火した場合の相手の反撃確率 : %s %%\n", (int)(100 * enemycounterchance));
			}
			// 特に急ぐ必要がない場合
			
			// ここから探索
			// 各種設定
			int searchdepth = 5; // 7
			int searchwidth = 10; // 50 ソートの手間があるのでO(NlogN)
			double timelimit = 0.96; // 0.9秒経過時点で探索打ち切り
			
			// 初期化
			double endtime = start + timelimit * 1000;
			
			
			Random rd = new Random();
			Function<FieldInfo, int[][]> RandomTsumo = (firstfield) -> {
				int[][] tsumo = new int[searchdepth][2];
				tsumo[0][0] = firstfield.firstpuyo;
				tsumo[0][1] = firstfield.secondpuyo;
				tsumo[1][0] = firstfield.nextfirstpuyo;
				tsumo[1][1] = firstfield.nextsecondpuyo;
				tsumo[2][0] = firstfield.nextnextfirstpuyo;
				tsumo[2][1] = firstfield.nextnextsecondpuyo;
				for (int i=3;i<searchdepth;i++) {
					tsumo[i][0] = rd.nextInt(5) + 1;
					if (rd.nextInt(3) == 0) {
						// 1/3の確率で同じ色のぷよを出す
						// 実際の確率とずれるので評価値は加重平均する
						tsumo[i][1] = tsumo[i][0];
					}
					else {
						tsumo[i][1] = (rd.nextInt(4) + tsumo[i][0]) % 5 + 1; // firstpuyoと異なる色をランダムに出す
					}
				}
				return tsumo;
			};

			BiFunction<FieldInfo, Integer, int[][]> IndextoTsumo = (firstfield, idx) -> {
				int[][] tsumo = new int[searchdepth][2];
				tsumo[0][0] = firstfield.firstpuyo;
				tsumo[0][1] = firstfield.secondpuyo;
				tsumo[1][0] = firstfield.nextfirstpuyo;
				tsumo[1][1] = firstfield.nextsecondpuyo;
				tsumo[2][0] = firstfield.nextnextfirstpuyo;
				tsumo[2][1] = firstfield.nextnextsecondpuyo;
				int div = 1;
				for (int i=3;i<searchdepth;i++) {
					int tsumoidx = (idx / div) % 15;
					tsumo[i] = bnf.TsumoIndextoTsumo(tsumoidx);
					div *= 15;
				}
				return tsumo;
			};
			
			int searchcount = 0;
			int truesearchcount = 0;
			FieldInfo[] firstfields = bnf.ThinkNextActionsAndCalc(myfirstfield);
			double[] actionsevaluation = new double[firstfields.length];
			int[] actionselectcount = new int[firstfields.length];

			double searchstarttime = System.currentTimeMillis();
			double maxsearchnum = Math.pow(15, searchdepth-3);
			double sumwidth = 0;
			while (System.currentTimeMillis() < endtime && truesearchcount < maxsearchnum) {
				// int[][] tsumo = RandomTsumo.apply(myfirstfield);
				int[][] tsumo = IndextoTsumo.apply(myfirstfield, truesearchcount);
				int[][] tsumo2 = new int[tsumo.length - 1][];
				for (int i=1;i<tsumo.length;i++) {
					tsumo2[i - 1] = tsumo[i];
				}
				
				// 残り時間をもとにこのツモに対するビームサーチのサーチ幅を決める
				int width = searchwidth;
				if (truesearchcount != 0) {
					double usedtimeratio = (double)(System.currentTimeMillis() - searchstarttime) / (endtime - searchstarttime);
					double searchdoneratio = (double)truesearchcount / maxsearchnum;
					double speedbefore = searchdoneratio / usedtimeratio;
					double speedafter = (1 - searchdoneratio) / (1 - usedtimeratio);
					double averagewidth = sumwidth / truesearchcount;
					width = Math.min((int)(averagewidth * speedbefore / speedafter), searchwidth * 2);
				}
				sumwidth += width;
				
				
				boolean istimeout = false;
				double[] evaluations = new double[firstfields.length];
				for (int i=0;i<firstfields.length;i++) {
					if (System.currentTimeMillis() > endtime) {
						istimeout = true;
						break;
					}
					evaluations[i] = bnf.ChokudaiSearch(firstfields[i], tsumo2, width);
				}
				if (istimeout) {
					// 時間を超えたら直前まで考えていたツモの結果は破棄する
					break;
				}
				int tsumomulti = 1;
				for (int i=3;i<searchdepth;i++) {
					tsumomulti *= tsumo[i][0] == tsumo[i][1] ? 1 : 2;
				}
				searchcount += tsumomulti;
				truesearchcount++;
				actionsevaluation= np.add(actionsevaluation, np.product(evaluations, tsumomulti));
				actionselectcount[np.maxindex(evaluations)] += tsumomulti;
			}
			for (int i=0;i<myactionnum;i++) {
				actionsevaluation[i] = actionsevaluation[i] / searchcount;//  + mycounterchances[i];
			}
			int selectindex = np.max(actionsevaluation) < 0 ? np.maxindex(actionselectcount) : np.maxindex(actionsevaluation);
			System.out.printf("相手の評価値: %s\n", (int)((bnf.EvaluateFieldFast(enemyfirstfield) + 0.6 * (searchdepth - 1)) * 100) / 100.0);
			System.out.printf("現在の評価値: %s\n", (int)(np.max(actionsevaluation) * 100) / 100.0);
			// System.out.printf("試行したツモのパターン数: %s\n", truesearchcount);
			// System.out.printf("試行したツモの実質パターン数: %s\n", searchcount);
			System.out.printf("平均サーチ幅: %s\n", (int)(sumwidth / truesearchcount * 100) / 100.0);
			if (mycounterchances[selectindex] != 1) {
				if (selectindex == defenceactionindex) {
					System.out.println("防御策と評価値を最大にする手が一致しました");
					System.out.printf("相手がこのターンで発火した場合の自分の反撃確率 : %s %%\n", (int)(100 * mycounterchance));
				}
				else {
					System.out.printf("相手がこのターンで発火した場合の自分の反撃確率 : %s %%\n", (int)(100 * mycounterchances[selectindex]));
					System.out.printf("相手がこのターンで発火し、自分は防御策を取っていた場合の反撃確率 : %s %%\n", (int)(100 * mycounterchance));
				}
			}

			/*
			System.out.printf("対象関数呼び出し回数 : %s\n", bnf.callcount);
			System.out.printf("対象関数1回あたり実行時間 : %s ns\n", bnf.sumtime / bnf.callcount);
			System.out.printf("対象関数合計実行時間 : %s ms\n", bnf.sumtime / 1.0e6);
			bnf.callcount = 0;
			bnf.sumtime = 0;
			*/
			
			long end = System.currentTimeMillis();
			System.out.println(end - start);
			System.out.println("--------------------------------");
			// ここまで探索
			
			PuyoDirection selectdirection = PuyoDirection.values()[savedactions[selectindex][0]];
			int selectcolumn = savedactions[selectindex][1];
			return new Action(selectdirection, selectcolumn);
		}
	}
	
	public int CountBlank() {
		Field f = getMyBoard().getField();
		int width = f.getWidth();
		int height = f.getHeight() - 2;
		int output = 0;
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				if (f.getPuyoType(i, j) == null) {
					output++;
				}
			}
		}
		return output;
	}
	
	private String TsumotoStr(boolean[] usetsumolist) {
		String out = "";
		for (int i=0;i<usetsumolist.length;i++) {
			if (usetsumolist[i]) {
				int[] tsumo = bnf.TsumoIndextoTsumo(i);
				out += "[" + PuyoNumbertoStr(tsumo[0]) + ", " + PuyoNumbertoStr(tsumo[1]) + "], ";
			}
		}
		return out;
	}
	
	private String PuyoNumbertoStr(int puyonum) {
		switch (puyonum) {
		case 1:
			return "青";
		case 2:
			return "緑";
		case 3:
			return "紫";
		case 4:
			return "赤";
		case 5:
			return "黄";
		default:
			return "";
		}
	}
	
	private void DrawField(long[] inputfield) {
		long[] field = Arrays.copyOf(inputfield, 6);
		for (int i=0;i<6;i++) {
			String columnstr = "";
			for (int j=0;j<12;j++) {
				for (int k=0;k<5;k++) {
					if ((field[i] & 1) != 0) {
						columnstr += PuyoNumbertoStr(k + 1);
					}
					field[i] >>= 1;
				}
			}
			System.out.println(columnstr);
		}
	}
	
	/**
	 * おまじない
	 * @param args
	 */
	public static void main(String args[]) {
		AbstractPlayer player1 = new MeowBeam();
		PuyoPuyo puyopuyo = new PuyoPuyo(player1);
		puyopuyo.puyoPuyo();
	}
}