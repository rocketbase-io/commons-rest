# commons-rest docs

Documentation site for [commons-rest](https://github.com/rocketbase-io/commons-rest), built
with [Astro Starlight](https://starlight.astro.build/) — deployed to
[commons-rest.rocketbase.io](https://commons-rest.rocketbase.io).

## Commands

All commands are run from the `docs/` directory:

| Command        | Action                                       |
| :------------- | :------------------------------------------- |
| `pnpm install` | Install dependencies                         |
| `pnpm dev`     | Start local dev server at `localhost:4321`   |
| `pnpm build`   | Build the production site to `./dist/`      |
| `pnpm preview` | Preview the built site locally               |

## Structure

- `src/content/docs/` — the pages (`.mdx`), one file per route
- `src/pages/releases.astro` — release history, fetched from the GitHub releases at build time
- `astro.config.mjs` — sidebar, site metadata, plugins
- `src/styles/custom.css` — rocketbase CI (fonts + brand colors)

The site documents **v4** (Spring Boot 4). For v3.x see the
[GitHub wiki](https://github.com/rocketbase-io/commons-rest/wiki).
