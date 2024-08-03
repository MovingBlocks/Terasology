## Writing Blog Posts

If you want to write a blog post for the [Terasology Website](https://terasology.org/) to highlight the latest contributions or community events, you can do so as follows:

1. Set yourself up for contributing to our [ModuleSite repo](https://github.com/MovingBlocks/ModuleSite)

   _You can use any contribution path you like. For example, you could fork and clone it, use the GitHub UI, or Codespaces._
1. Add a new directory in the [`blog` subdir](https://github.com/MovingBlocks/ModuleSite/tree/master/blog)

   _The directory name should comply with the following pattern: `YYYY-MM-DD-<title>`, e.g. `2023-01-16-my-fancy-blogpost`_
1. Optional: In your blog post directory, add a cover image (ideally in `.jpg` format)
1. In your blog post directory, create a file named `index.md`
   
   _The metadata section should be at the top of your `index.md`:_
   ```
   ---
   posttype: "blog"
   title: <title>
   cover: "./<cover-image-name>.jpg"
   description: <summary>
   author: <your-nickname>
   date: "YYYY-MM-DD"
   tags: [<tag-list>]
   ---
   ```
   _Valid tags are "TeraSaturday", "TeraSpotlight", "GSoC", "Update", "Project". If you are uncertain which one to use or want to propose a new one, ask the `@Publicity` folks in our #outreach channel on Discord._
1. Add your blog post content in markdown below the metadata section.
1. Open a PR with your changes.

**Note:** _Please check copyright of any external images you want to use, download them and commit them as part of your PR._
