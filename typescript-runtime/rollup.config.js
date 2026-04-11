import typescript from '@rollup/plugin-typescript';
import dts from 'rollup-plugin-dts';

export default [
  // ESM + CJS builds
  {
    input: 'src/index.ts',
    output: [
      {
        file: 'dist/index.mjs',
        format: 'esm',
        sourcemap: true,
      },
      {
        file: 'dist/index.cjs',
        format: 'cjs',
        sourcemap: true,
        exports: 'named',
      },
    ],
    external: ['axios', 'react', 'react/jsx-runtime'],
    plugins: [
      typescript({
        tsconfig: './tsconfig.json',
        declaration: false,
      }),
    ],
  },
  // Type definitions bundle
  {
    input: 'src/index.ts',
    output: {
      file: 'dist/index.d.ts',
      format: 'esm',
    },
    external: ['axios', 'react'],
    plugins: [dts()],
  },
];
