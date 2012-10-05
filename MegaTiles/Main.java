/* MegaTiles v0.9 by Nikolay Parfenev, MP-54 */

import java.util.Random;
import java.util.Timer;

public class Main
{
	public static int hx; // координаты свободной клетки
	public static int hy;
	public static int fx; // координаты искомой пятнашки
	public static int fy;
	public static int length; // Длина стороны игрового поля с пятнашками
	public static int s_length; // Число клеток игрового поля
	public static int switches; // Число перестановок, выполненных в процессе сортировки
	public static int[] Line; // Линейный массив, хранящий матрицу, разложенную в линию 
	public static int[][] Tiles; // Матрица текущего состояния игрового поля
	public static boolean[][] Locked; // Флаги блокировки клеток игрового поля
	public static boolean flag; // Флаг форматирования
	
	public static void main(String[] args) {
		length=250; // Длина стороны игрового поля с пятнашками
		if (length<2) System.out.println("Length should be 2 or more");
		else {
			// Одинарный тест
			// test();
			
			// Непрерывный тест программы с увеличением длины
			 while (test()) length++;
			
			// Непрерывный тест программы без увеличения длины
			 //while (test());
		}		
	}
	
	/*  Тест программы. Выполнение программы состоит из трех основных этапов:
	 *    1) Генерация комбинации пятнашек - функция generate().
	 *    2) Оценка разрешимости данной комбинации пятнашек - функция solvable().
	 *    3) Решение - функция solve(). Если данная комбинация является
	 *          a) разрешимой: пятнашки собираются полностью
	 *          б) неразрешимой: пятнашки собираются почти полностью
	 *                (две пятнашки окажутся не на своих местах, причём известно какие - см. line 150)
	 *       В процессе решения на консоль выводится информация о текущем прогрессе выполнения.
	 *  
	 *  После окончания работы функции solve() проверяется, полностью ли собрались пятнашки.
	 *  Если оценка разрешимости не совпала с разрешимостью на практике,
	 *  тест считается не пройденным - функция test() вернет false().
	 *  
	 *  Перед выводом и после вывода текущего состояния игрового поля на консоль присутствует задержка для удобства чтения.
	 */
	
	public static boolean test() {
        
		System.out.println("===== "+Integer.toString(length)+"x"+Integer.toString(length)+" TILES TEST =====\n");
		
		System.out.print(" Generation: ");
		generate();
		System.out.println("COMPLETED");
		
		System.out.println(" Initial state:\n");
		
		try {
			Thread.sleep(3000L);
		}
		catch (Exception e) {}
		
		print_state();
		
		try {
			Thread.sleep(3000L);
		}
		catch (Exception e) {}
		
		System.out.print("Solvable: ");
		boolean solvable=solvable();
		System.out.println(Boolean.toString(solvable).toUpperCase());
		
		System.out.print("Attempting to Solve! Progress:   0%");
		boolean solved=solve();
		if (flag==true) System.out.print(" 100%");
		System.out.print("\nSolve   : ");
		if (solved) System.out.println("SUCCESS\n");
		else System.out.println("FAILURE\n");
		
		try {
			Thread.sleep(3000L);
		}
		catch (Exception e) {}
		
		System.out.println("End state:    ");
		print_state();
		System.out.println("");
		
		try {
			Thread.sleep(3000L);
		}
		catch (Exception e) {}
		
		return (solved==solvable);
	}
	
	// Главная функция "решение", с выводом на консоль информации о текущем прогрессе
	
	public static boolean solve() {
		
		int count;
		flag=false;
		double percents=0;
		double dif=(double)100/length;
		
		for (count=length-1; count>1; count--) {
			make_line(count);
			percents+=dif;
			System.out.print(" "+Integer.toString((int)percents)+"%");
		}
		
		for (count=1; count<length-3; count++) make_wall_right(length-count, 2*length-count, length-count);
		
		if (length>3) make_wall_right(length+3, length+2, 3);
		
		if (length>2) make_wall_left(length, length+1, 0);
		
		percents+=dif;
		System.out.print(" "+Integer.toString((int)percents)+"%");
		
		while (Tiles[1][0]!=1) {
			down();
			left();
			up();
			right();
		}
		
		if (length>3) Locked[3][1]=false;
		
		right();
		down();
		left();
		left();
		up();
		left();
		down();
		right();
		right();
		right();
		
		percents+=dif;
		System.out.print(" "+Integer.toString((int)percents)+"%");
		
		if ((int)percents!=100) flag=true;
		
		switch (length) {
			case 2: return ((Tiles[0][1]==2)&&(Tiles[1][1]==3));
			case 3: return ((Tiles[2][0]==2)&&(Tiles[2][1]==5));
			default: return ((Tiles[2][0]==2)&&(Tiles[3][0]==3));
		}
	}
	
	/* Главная функция "проверка на разрешимость".
	 * Матрица раскладывается в линейный массив и подсчитывается число перестановок в процессе сортировки.
	 * Если это число чётно, значит сгенерированная комбинация является разрешимой;
	 * в противном случае - неразрешимой.
	 */
	
	public static boolean solvable() {
		while (hx>0) right();
		while (hy>0) down();
		
		Line = new int[s_length];
		for (int i=0; i<s_length; i++) Line[i]=Tiles[i%length][i/length];
		
		switches=0;
		sort(Line, 0, s_length-1);
		
		if (switches%2==0) return true;
		else return false;
	}
	
	// Вспомогательная функция "сортировка разложенной в линию матрицы" с использованием метода Хоара.
	// В процессе сортировки производится подсчет перестановок.
	
	public static void sort(int Line[], int left, int right) {
		
	      int i=left, j=right;
	      int tmp;
	      int del = Line[(left+right)/2];
	      
	      while (i <= j) {
	            while (Line[i] < del) i++;
	            while (Line[j] > del) j--;
	            if (i <= j) {
	                  tmp = Line[i];
	                  Line[i] = Line[j];
	                  Line[j] = tmp;
	                  if (i!=j) switches++;
	                  i++;
	                  j--;
	            }
	      };
	
	      if (left < j) sort(Line, left, j);
	      if (i < right) sort(Line, i, right);
	}
	
	// Главная функция "сгенерировать комбинацию пятнашек"
	
	public static void generate() {
		
		int i, j;

		s_length=length*length;
		
		Tiles = new int[length][length];
		Locked = new boolean[length][length];
		
		boolean[] Used = new boolean[s_length];
	
		for (i=0; i<s_length; i++) Used[i]=false;
		for (i=0; i<length; i++) for (j=0; j<length; j++) Locked[i][j]=false;
		
		Random r = new Random();
		
		for (i=0; i<s_length; ) {
			j = r.nextInt(s_length);
			if (Used[j]==false) {
				Tiles[i/length][i%length]=j;
				Used[j]=true;
				if (j==0) {
					hx=i/length;
					hy=i%length;
				}
				i++;
			}
		}
	}
	
	// Функция третьего уровня "собрать левую стенку с номером столбца num из пятнашек t1 и t2"

	public static void make_wall_left(int t1, int t2, int num) {
		
		find_tile(t2);
		get_close(fx,fy);
		
		while (fy!=0) {
			move_up(fx,fy);
			fy-=1;
		}
		
		while (fx!=num) {
			move_left(fx,fy);
			fx-=1;
		}
		
		find_tile(t1);
		
		if ((fy<2)&&(fx<num+2)) {
			if ((hy==0)&&(fx==num+1)) {
				up();
				fy-=1;
			}
			if (fy==0) {
				right();
				down();
				left();
			}
			
			else {
				if (hy==0) {
					up();
					right();
				}
				if (fx==num) right();
				down();
				left();
				left();
				up();
				right();
				down();
				right();
				up();
				left();
				down();
				left();
				up();
				right();
				down();
				left();
				up();
				right();
				right();
				down();
				left();
			}
		}
		else {
			
			if (hx==num) left();
			get_close(fx,fy);
			
			while (fy!=0) {
				move_up(fx,fy);
				fy-=1;
			}
			
			while (fx!=num+1) {
				move_left(fx,fy);
				fx-=1;
			}
			
			if (hy==0) {
				up();
				right();
			}
			right();
			down();
			left();
		}
		
		Locked[num][0]=true;
		Locked[num][1]=true;
		
	}
	
	// Функция третьего уровня "собрать правую стенку с номером столбца num из пятнашек t1 и t2"

	public static void make_wall_right(int t1, int t2, int num) {
		find_tile(t2);
		get_close(fx,fy);
		
		while (fy!=0) {
			move_up(fx,fy);
			fy-=1;
		}
		
		while (fx!=num) {
			move_right(fx,fy);
			fx+=1;
		}
		
		find_tile(t1);
		
		if ((fy<2)&&(fx>num-2)) {
			if ((hy==0)&&(fx==num-1)) {
				up();
				fy-=1;
			}
			if (fy==0) {
				left();
				down();
				right();
			}
			
			else {
				if (hy==0) {
					up();
					left();
				}
				if (fx==num) left();
				down();
				right();
				right();
				up();
				left();
				down();
				left();
				up();
				right();
				down();
				right();
				up();
				left();
				down();
				right();
				up();
				left();
				left();
				down();
				right();
			}
		}
		else {
			
			if (hx==num) right();
			
			get_close(fx,fy);
			
			while (fy!=0) {
				move_up(fx,fy);
				fy-=1;
			}
			
			while (fx!=num-1) {
				move_right(fx,fy);
				fx+=1;
			}
			
			if (hy==0) {
				up();
				left();
			}
			left();
			down();
			right();
		}
		Locked[num][0]=true;
		Locked[num][1]=true;
	}
	
	// Функция третьего уровня "собрать линию с номером ряда num"
	
	public static void make_line(int num) {
		
		for (int c=length-1; c>1; c--) {
			find_tile(length*num+c);
			get_close(fx,fy);
			
			while (fx>c) {
				move_left(fx,fy);
				fx-=1;
			}
			while (fy!=num) {
				move_down(fx,fy);
				fy+=1;
			}
			while (fx<c) {
				move_right(fx,fy);
				fx+=1;
			}
			
			if (hy==num) down();
			Locked[c][num]=true;
		}
		
		find_tile(length*num+1);
		get_close(fx,fy);
		
		while (fx!=0) {
			move_left(fx,fy);
			fx-=1;
		}
		
		while (fy!=num) {
			move_down(fx,fy);
			fy+=1;
		}
		
		find_tile(length*num);
		
		if ((fx<2)&&(fy>num-2)) {
			if ((hx==0)&&(fy==num-1)) {
				left();
				fx-=1;
			}
			if (fx==0) {
				up();
				right();
				down();
			}
			
			else {
				if (hx==0) {
					left();
					up();
				}
				if (fy==num) up();
				right();
				down();
				down();
				left();
				up();
				right();
				up();
				left();
				down();
				right();
				down();
				left();
				up();
				right();
				down();
				left();
				up();
				up();
				right();
				down();
			}
		}
		else {
			
			if (hy==num) down();
			get_close(fx,fy);
			
			while (fx!=0) {
				move_left(fx,fy);
				fx-=1;
			}
			
			while (fy!=num-1) {
				move_down(fx,fy);
				fy+=1;
			}
			
			if (hx==0) {
				left();
				up();
			}
			up();
			right();
			down();
		}
		
		Locked[1][num]=true;
		Locked[0][num]=true;
	}
	
	// Функция второго уровня "передвинуть вверх близлежащую пятнашку с координатами tx и ty"
	
	public static void move_up(int tx, int ty) {   
		if (hx==tx) {   
			if (hy<ty) {
				while (hy!=ty) up(); 
				return;
			}
			else if (!right()) left();  
		}
		while (hy>ty-1) down();  
		if (hx>tx) right();   
		else left();
		up();
	}
	
	// Функция второго уровня "передвинуть влево близлежащую пятнашку с координатами tx и ty"
	
	public static void move_left(int tx, int ty) {
		if (hy==ty) {
			if (hx<tx) {
					while (hx!=tx) left();
					return;
			}
			else if (!down()) up();
		}
		while (hx>tx-1) right();
		if (hy>ty) down();
		else up();
		left();
	}
	
	// Функция второго уровня "передвинуть вправо близлежащую пятнашку с координатами tx и ty"
	
	public static void move_right(int tx, int ty) {  
		if (hy==ty) {  
			if (hx>tx) { 
				while (hx!=tx) right();    
				return;
			}  
			else if (!down()) up();  
		}
		while (hx<tx+1) left();  
		if (hy>ty) down(); 
		else up();
		right();  
	}
	
	// Функция второго уровня "передвинуть вниз близлежащую пятнашку с координатами tx и ty"
	
	public static void move_down(int tx, int ty) {  
		if (hx==tx) {  
			if (hy>ty) {  
				while (hy!=ty) {
					down();
				}
				return;  
				}   
			else {
				if (!right()) left();
			}
			}  
		if (hx>tx) {   
			if (Locked[tx+1][ty+1]) {   
				if (hy==ty) {
					down();
				}
				right();
				right();
			}
			else {
				while (hy<ty+1) up();
				right();
				down();
				return;
			}
		}  
		while (hy<ty+1) up();
		left();
		down();
	}
	
	// Функция второго уровня "найти пятнашку со значением value"
	
	public static void find_tile(int value) {
		for (int j=0; j<length; j++) {
			for (int i=0; i<length; i++) {
				if (Tiles[i][j]==value) {
					fx=i;
					fy=j;
					return;
				}
			}
		}
	}
	
	// Функция второго уровня "приблизиться к пятнашке с координатами tx и ty"
	
	public static void get_close(int tx, int ty) {
		
		while (hy>ty+1) down();
		
		while (hx<tx-1) {
			if (!Locked[hx+1][hy]) left();
			else break;
		}
		
		while (hy<ty-1) {
			if (!Locked[hx][hy+1]) up();
			else break;
		}
		
		while (hx>tx+1) right();
	}
	
	// Функция первого уровня "сдвинуть вверх"
	
	public static boolean up() { 
		if ((hy==length-1)||(Locked[hx][hy+1])) return false;
		else {
			Tiles[hx][hy]=Tiles[hx][hy+1];
			hy+=1;
			Tiles[hx][hy]=0;
			return true;
		}
	}
	
	// Функция первого уровня "сдвинуть влево"
	
	public static boolean left() {   
		if ((hx==length-1)||(Locked[hx+1][hy])) return false;
		else {
			Tiles[hx][hy]=Tiles[hx+1][hy];
			hx+=1;
			Tiles[hx][hy]=0;
			return true;
		}
	}
	
	// Функция первого уровня "сдвинуть вниз"
	
	public static boolean down() {  
		if (hy==0) return false;
		else {
			Tiles[hx][hy]=Tiles[hx][hy-1];
			hy-=1;
			Tiles[hx][hy]=0;
			return true;
		} 
	}
	
	// Функция первого уровня "сдвинуть вправо"
	
	public static boolean right() {
		if (hx==0) return false;
		else {
			Tiles[hx][hy]=Tiles[hx-1][hy];
			hx-=1;
			Tiles[hx][hy]=0;
			return true;
		}
	}

	// Функция вывода на консоль текущего состояния игрового поля, с форматированием
	
	public static void print_state() {
		int spaces=Integer.toString(s_length).length();
		for (int j=0; j<length; j++) {   
			for (int i=0; i<length; i++) {
				for (int count=spaces-Integer.toString(Tiles[i][j]).length()+1; count>0; count--) System.out.print(" ");
				System.out.print(Integer.toString(Tiles[i][j]));
			}    
			System.out.println("");
		}
		System.out.println("");
	}
}