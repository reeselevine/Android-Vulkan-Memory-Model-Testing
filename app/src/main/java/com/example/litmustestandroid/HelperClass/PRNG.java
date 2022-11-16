package com.example.litmustestandroid.HelperClass;

import java.util.Random;

/** A simple custom PRNG, used to match random generation across this app and gpuharbor. */
public class PRNG extends Random {

    private int _seed;

    public PRNG(String seed) {
       this._seed = seed.hashCode();
       if (this._seed <= 0) this._seed += 2147483646;
    }

    public PRNG(int seed) {
        this._seed = seed;
        if (this._seed <= 0) this._seed += 2147483646;
    }

    @Override
    public int nextInt() {
       this._seed = (this._seed * 16807) % 2147483646;
       if (this._seed < 0) this._seed += 2147483646;
       return this._seed;
    }
}
