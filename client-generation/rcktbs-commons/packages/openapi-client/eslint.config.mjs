import nx from '@nx/eslint-plugin';
import unusedImports from 'eslint-plugin-unused-imports';
import baseConfig from '../../eslint.config.mjs';

export default [
  ...baseConfig,
  ...nx.configs['flat/react'],
  {
    files: ['**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx'],
    plugins: {
      'unused-imports': unusedImports,
    },
    // Override or add rules here
    rules: {
      '@typescript-eslint/no-empty-interface': 'off',
      '@typescript-eslint/no-unused-vars': 'off', // Disable default unused vars
      'unused-imports/no-unused-imports': 'error', // Remove unused imports
      'unused-imports/no-unused-vars': [
        'warn',
        {
          'vars': 'all',
          'varsIgnorePattern': '^_',
          'args': 'after-used',
          'argsIgnorePattern': '^_'
        }
      ],
      'typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
    },
  },
];
