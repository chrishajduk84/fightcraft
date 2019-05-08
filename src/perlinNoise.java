/**
 * @author Chris Hajduk
 *
 * Started June 1, 2012
 */

public class perlinNoise {

	int seed;
	int numberOfFunctions = 10; //Number of functions to add together
	float persistence = 0.9f; //Depreciation of amplitude of each consecutive function 

	//This code for generating numbers is complex, and I still do not fully understand it
	//Thanks to http://freespace.virgin.net/hugo.elias/models/m_perlin.htm for the pseudocode


	public float cosInterpolate(float a, float b, float x)
	{
		//Interpolation is the process of getting a value between a and b
		//When x is 1, b is returned, when x is 0, a is returned
		//This smoothens the result by adding two functions together
		x = (float) Math.cos(x * Math.PI);


		return a * (1 - x) + b * x;
	}
	
	public float linearInterpolate(float a, float b, float x)
	{
		return a * (1 - x) + b * x;
	}
	
	public float cubeicInterpolate(float v0, float v1, float v2, float v3, float x)
	{
		//Interpolation is the process of getting a value between a and b
		//When x is 1, b is returned, when x is 0, a is returned
		//This smoothens the result by adding two functions together
		float A = (v3 - v2) - (v0 - v1);
		float B = (v0 - v1) - A;
		float C = (v2 - v0);
		float D = v1;
		


		return (float) (A * Math.pow(x, 3) + B * Math.pow(x, 2) + C * x + D);
	}
	
	public float smoothNoise(int x,int y)
	{
		//Noise is returned based on a grid
		//Smoothed by taking the average of frequencies surrounding the 2d grid
//		float cornersFurther4 = (Noise(x - 5, y - 5, seed) + Noise(x + 5, y - 5, seed) + Noise(x - 5, y + 5, seed) + Noise(x + 5, y + 5, seed)) / 5;
//		float sidesFurther4 = (Noise(x - 5, y, seed) + Noise(x + 5, y, seed) + Noise(x, y + 5, seed) + Noise(x, y - 5, seed)) / 5;
//		float cornersFurther3 = (Noise(x - 4, y - 4, seed) + Noise(x + 4, y - 4, seed) + Noise(x - 4, y + 4, seed) + Noise(x + 4, y + 4, seed)) / 4;
//		float sidesFurther3 = (Noise(x - 4, y, seed) + Noise(x + 4, y, seed) + Noise(x, y + 4, seed) + Noise(x, y - 4, seed)) / 4;
//		float cornersFurther2 = (Noise(x - 3, y - 3, seed) + Noise(x + 3, y - 3, seed) + Noise(x - 3, y + 3, seed) + Noise(x + 3, y + 3, seed)) / 3;
//		float sidesFurther2 = (Noise(x - 3, y, seed) + Noise(x + 3, y, seed) + Noise(x, y + 3, seed) + Noise(x, y - 3, seed)) / 3;
//		float cornersFurther = (Noise(x - 2, y - 2, seed) + Noise(x + 2, y - 2, seed) + Noise(x - 2, y + 2, seed) + Noise(x + 2, y + 2, seed)) / 3;
//		float sidesFurther = (Noise(x - 2, y, seed) + Noise(x + 2, y, seed) + Noise(x, y + 2, seed) + Noise(x, y - 2, seed)) / 3;
		float corners = (Noise(x - 1, y - 1, seed) + Noise(x + 1, y - 1, seed) + Noise(x + 1, y + 1, seed) + Noise(x - 1, y - 1, seed)) / 2;
		float sides = (Noise(x - 1, y, seed) + Noise(x + 1, y, seed) + Noise(x, y + 1, seed) + Noise(x, y - 1, seed)) / 2;
		float center = Noise(x,y,seed) / 2;
		return (corners + sides + center)/3; //+ sidesFurther + cornersFurther + sidesFurther2 + cornersFurther2 + sidesFurther3 + cornersFurther3 + sidesFurther4 + cornersFurther4) / 11;
	}
	public float Noise(int x, int y, int seed)
	{
		//Based on a formula I found online
		//Includes finding remainder of prime number division
		//Generates seeded (random values) between 0 and 1.
		int n = x + y * seed;
		n = (n<<13) ^ n;
		return (float)(1.0 - ((n*(n * n * 15731 + 789221) + 1376312589) % 2147483647) / 1073741824.0);

	}
	public void generateRandomSeed()
	{
		seed = (int)(Math.random() * 1000000) + 1;
	}

	public float InterpolatedNoise(float x, float y)
	{
		int intX = (int)x;
		int intY = (int)y;
		
		float fracX = x - intX;
		float fracY = y - intY;
		
		float vertex1 = smoothNoise(intX,intY);
		float vertex2 = smoothNoise(intX + 1, intY);
		float vertex3 = smoothNoise(intX,intY + 1);
		float vertex4 = smoothNoise(intX + 1, intY + 1);
		
		float i1 = cosInterpolate(vertex1,vertex2,fracX);
		float i2 = cosInterpolate(vertex3,vertex4,fracX);
		return cosInterpolate(i1,i2,fracY);
	}
	
	
	public float getNoise(float x, float y, float persist, int functions)
	{
		float total = 0;
		persistence = persist;
		numberOfFunctions = functions;
		for (int i = 0; i < numberOfFunctions; i++)
		{
			float amplitude = (float)Math.pow(persistence, i);
			float frequency =  (float) Math.pow(2, i);
			
			total += InterpolatedNoise(x * frequency, y * frequency) * amplitude;
		}
		return total;
	}
}
