package meow2021;

import java.util.Arrays;
import java.util.Comparator;

import maou2020.TriConsumer;

public class Np {
	private double[] castarray(int[] x) {
		double[] y = new double[x.length];
		for (int i=0;i<x.length;i++) {
			y[i] = x[i];
		}
		return y;
	}
	
	public int max(int[] x) {
		int maxnum = x[0];
		for (int i=1;i<x.length;i++) {
			if (x[i] > maxnum) {
				maxnum = x[i];
			}
		}
		return maxnum;
	}
	
	public double min(double[] x) {
		double minnum = x[0];
		for (int i=1;i<x.length;i++) {
			if (x[i] < minnum) {
				minnum = x[i];
			}
		}
		return minnum;
	}
	
	public int min(int[] x) {
		int minnum = x[0];
		for (int i=1;i<x.length;i++) {
			if (x[i] < minnum) {
				minnum = x[i];
			}
		}
		return minnum;
	}
	
	public double max(double[] x) {
		double maxnum = x[0];
		for (int i=1;i<x.length;i++) {
			if (x[i] > maxnum) {
				maxnum = x[i];
			}
		}
		return maxnum;
	}
	
	public int maxindex(double[] x) {
		double maxnum = x[0];
		int maxidx = 0;
		for (int i=1;i<x.length;i++) {
			if (x[i] > maxnum) {
				maxnum = x[i];
				maxidx = i;
			}
		}
		return maxidx;
	}
	
	public int maxindex(int[] x) {
		int maxnum = x[0];
		int maxidx = 0;
		for (int i=1;i<x.length;i++) {
			if (x[i] > maxnum) {
				maxnum = x[i];
				maxidx = i;
			}
		}
		return maxidx;
	}
	
	public double[] add(double[] x, double[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y[i];
		}
		return z;
	}

	public double[] add(int[] x, double[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y[i];
		}
		return z;
	}
	
	public double[] add(double[] x, int[] y) {
		return add(y, x);
	}

	public int[] add(int[] x, int[] y) {
		assert x.length == y.length;
		int[] z = new int[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y[i];
		}
		return z;
	}
	
	public double[] add(double[] x, int y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y;
		}
		return z;
	}

	public double[] add(int x, double[] y) {
		return add(y, x);
	}

	public double[] add(int[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y;
		}
		return z;
	}

	public double[] add(double x, int[] y) {
		return add(y, x);
	}
	
	public int[] add(int[] x, int y) {
		int[] z = new int[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] + y;
		}
		return z;
	}

	public int[] add(int x, int[] y) {
		return add(y, x);
	}

	public double[] sub(double[] x, double[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y[i];
		}
		return z;
	}

	public double[] sub(int[] x, double[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y[i];
		}
		return z;
	}
	
	public double[] sub(double[] x, int[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y[i];
		}
		return z;
	}

	public int[] sub(int[] x, int[] y) {
		assert x.length == y.length;
		int[] z = new int[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y[i];
		}
		return z;
	}
	
	public double[] sub(double[] x, int y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y;
		}
		return z;
	}	
	
	public double[] sub(double[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y;
		}
		return z;
	}

	public double[] sub(int x, double[] y) {
		double[] z = new double[y.length];
		for (int i=0;i<y.length;i++) {
			z[i] = x - y[i];
		}
		return z;
	}

	public double[] sub(int[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] - y;
		}
		return z;
	}

	public double[] sub(double x, int[] y) {
		double[] z = new double[y.length];
		for (int i=0;i<y.length;i++) {
			z[i] = x - y[i];
		}
		return z;
	}
	
	public double sum(double[] x) {
		double sumnum = 0;
		for (int i=0;i<x.length;i++) {
			sumnum += x[i];
		}
		return sumnum;
	}
	
	public int sum(int[] x) {
		int sumnum = 0;
		for (int i=0;i<x.length;i++) {
			sumnum += x[i];
		}
		return sumnum;
	}
	
	public int sum(boolean[] x) {
		int sumnum = 0;
		for (int i=0;i<x.length;i++) {
			sumnum += x[i] ? 1 : 0;
		}
		return sumnum;
	}
	
	public double[] product(double[] x, double[] y) {
		assert x.length == y.length;
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] * y[i];
		}
		return z;
	}
	
	public int[] product(int[] x, int[] y) {
		assert x.length == y.length;
		int[] z = new int[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] * y[i];
		}
		return z;
	}
	
	public double[] product(double[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] * y;
		}
		return z;
	}
	
	public double[] product(double x, double[] y) {
		return product(y, x);
	}
	
	public double[] product(int[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = x[i] * y;
		}
		return z;
	}
	
	public int[] arange(int x) {
		int[] y = new int[x];
		for (int i=0;i<x;i++) {
			y[i] = i;
		}
		return y;
	}
	
	public double[] pow(double[] x, double y) {
		double[] z = new double[x.length];
		for (int i=0;i<x.length;i++) {
			z[i] = Math.pow(x[i], y);
		}
		return z;
	}
	
	public double[] pow(double x, double[] y) {
		double[] z = new double[y.length];
		for (int i=0;i<y.length;i++) {
			z[i] = Math.pow(x, y[i]);
		}
		return z;
	}
	
	public double[] pow(double x, int[] y) {
		return pow(x, castarray(y));
	}

	public double[] product(double x, int[] y) {
		return product(y, x);
	}
	
	public double dot(double[] x, double[]y) {
		return sum(product(x, y));
	}
	
	public double dot(int[] x, double[]y) {
		return sum(product(castarray(x), y));
	}
	
	public double dot(double[] x, int[]y) {
		return sum(product(x, castarray(y)));
	}
	
	public int dot(int[] x, int[]y) {
		return sum(product(x, y));
	}
	
    public static int[] argsort(double[] a) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return Double.compare(a[i1], a[i2]);
            }
        });
        return asarray(indexes);
    }
    
    public int[] argsort(int[] a) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return Double.compare(a[i1], a[i2]);
            }
        });
        return asarray(indexes);
    }

    private static <T extends Number> int[] asarray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
    
    public double[] applyindex(double[] x, int[] index) {
    	double[] y = new double[index.length];
    	for (int i=0;i<index.length;i++) {
    		y[i] = x[index[i]];
    	}
    	return y;
    }
    
    public double[] reverse(double[] x) {
    	double[] y = new double[x.length];
    	for (int i=0;i<x.length;i++) {
    		y[x.length - i - 1] = x[i];
    	}
    	return y;
    }
    
    public double[] cumsum(double[] x) {
    	double[] y = new double[x.length];
    	double temp = 0;
    	for (int i=0;i<x.length;i++) {
    		temp += x[i];
    		y[i] = temp;
    	}
    	return y;
    }
    
    public boolean[] dainari(double[] x, double y) {
    	boolean[] z = new boolean[x.length];
    	for (int i=0;i<x.length;i++) {
    		z[i] = x[i] > y;
    	}
    	return z;
    }

    public boolean[] shounari(double[] x, double y) {
    	boolean[] z = new boolean[x.length];
    	for (int i=0;i<x.length;i++) {
    		z[i] = x[i] < y;
    	}
    	return z;
    }
    
    public double pow(double x, double y) {
    	return Math.pow(x, y);
    }
    
    public int pow(int x, int y) {
    	assert y >= 0;
    	int z = 1;
    	for (int i=0;i<y;i++) {
    		z *= x;
    	}
    	return z;
    }
    
    public double[] slice(double[] x, boolean[] y) {
    	assert x.length == y.length;
    	double[] z = new double[sum(y)];
    	int j = 0;
    	for (int i=0;i<x.length;i++) {
    		if (y[i]) {
    			z[j] = x[i];
    			j++;
    		}
    	}
    	return z;
    }
    
    public double[] ReLU(double[] x) {
    	double[] y = new double[x.length];
    	for (int i=0;i<x.length;i++) {
    		y[i] = x[i] > 0 ? x[i] : 0;
    	}
    	return y;
    }
    
    public boolean[] OnesBool(int x) {
    	boolean[] y = new boolean[x];
    	for (int i=0;i<x;i++) {
    		y[i] = true;
    	}
    	return y;
    }
    
}
