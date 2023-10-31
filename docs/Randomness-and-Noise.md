# Randomness and Noise

Random numbers play a major role in procedural terrain generation and many other dynamically created content in the game. There are different random number generators and different types of noise.

## Random Numbers

There are two implementations of the `Random` interface: `FastRandom` and `MersenneRandom`. As you might expect, the first one is rather simplistic, which makes it very fast. In some cases, the quality is not sufficient though and we recommend the implementation that is based on the Mersenne prime number twister. It is very close to *real* noise, but more expensive to compute.

**Rule of thumb:** Use `MersenneRandom` when looking at very small value ranges (e.g. floats between 0 and 0.000001 or boolean values), FastRandom otherwise.


## Noise

Noise generators are similar to random number generators, but provide a deterministic value per coordinate in space. 

The `PerlinNoise` and `SimplexNoise` classes assign random gradient in a regular grid (Perlin uses squares/cubes, Simplex uses triangles/tetrahedrons) and interpolate in between. Simplex is a bit faster than Perlin, in particular for higher dimensions at comparable noise quality. Noise is isotropic (looks the same independent from direction or position).

The `BrownianNoise` class integrates values from other noise implementations. This allows for adjustment of noise frequencies. For example, different layers of Perlin noise can be put on top of each other at different spatial scales and at different amplitudes. This gives the prominent Perlin noise textures.

The `FastNoise` class is a bit different as it works on discrete numbers. This is good enough for per-block noise values. It is about 2x faster than SimplexNoise and 5x faster than PerlinNoise. Noise values repeat after 256, i.e. noise(256) is equal to noise(0).

**Rule of thumb:** Use `SimplexNoise` whenever possible. Noise that is required per block can also be computed using `FastNoise`.

![An overview over different noise implementations](https://cloud.githubusercontent.com/assets/1820007/5960183/f31d5402-a7d3-11e4-90f3-6bee10c7d2ce.png)