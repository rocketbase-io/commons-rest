// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightLlmsTxt from 'starlight-llms-txt';

// https://astro.build/config
export default defineConfig({
	site: 'https://commons-rest.rocketbase.io',
	integrations: [
		starlight({
			title: 'commons-rest',
			description:
				'Spring Boot library for CRUD REST services — Read/Write DTO separation, abstract controllers, RFC 9457 problem details and generated TypeScript clients.',
			logo: {
				light: './src/assets/logomark.svg',
				dark: './src/assets/logomark-dark.svg',
			},
			social: [
				{
					icon: 'github',
					label: 'GitHub',
					href: 'https://github.com/rocketbase-io/commons-rest',
				},
			],
			editLink: {
				// TODO: switch back to /edit/master/docs/ once v4-beta is merged
				baseUrl: 'https://github.com/rocketbase-io/commons-rest/edit/v4-beta/docs/',
			},
			tableOfContents: false,
			plugins: [
				starlightLlmsTxt({
					description:
						'Spring Boot library for CRUD REST services with Read/Write DTO separation, RFC 9457 problem details, hashids/TSID id handling and generated TypeScript clients. Maven: io.rocketbase.commons:commons-rest-server',
				}),
			],
			customCss: ['./src/styles/custom.css'],
			sidebar: [
				{
					label: 'Start Here',
					items: ['getting-started', 'concepts'],
				},
				{
					label: 'Guides',
					items: [
						'pagination',
						'error-handling',
						'i18n',
						'obfuscated-ids',
						'tsid',
						'typescript-clients',
						'logging',
						'error-pages',
						'utilities',
					],
				},
				{
					label: 'Examples',
					items: ['sample-application'],
				},
				{
					label: 'Reference',
					items: ['configuration', 'migration-v4'],
				},
				{ label: 'Releases', link: '/releases/' },
			],
		}),
	],
});
