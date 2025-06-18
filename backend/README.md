# MovieSwipe Backend

This is the backend for MovieSwipe, a group movie recommendation and voting app. It is built with Node.js, TypeScript, Express, MongoDB, and integrates with the TMDb API for movie data. All endpoints are documented with OpenAPI (Swagger).

## Features
- Google token verification for authentication
- Group management (create, join, delete, invite)
- User genre preferences
- Voting sessions and movie voting
- Intelligent movie recommendation algorithm
- TMDb API integration for movie data
- OpenAPI documentation
- Azure deployment scripts (Dockerfile, ARM template)

## Setup
1. Install dependencies:
   ```sh
   npm install
   ```
2. Create a `.env` file in the root with the following variables:
   ```env
   MONGODB_URI=mongodb://<username>:<password>@<host>:<port>/<database>
   TMDB_API_KEY=your_tmdb_api_key
   GOOGLE_CLIENT_ID=your_google_client_id
   PORT=3000
   ```
   - [Get a TMDb API key](https://www.themoviedb.org/settings/api)
   - [Get a Google Client ID](https://console.developers.google.com/apis/credentials)
3. Run the server in development mode:
   ```sh
   npx ts-node src/index.ts
   ```

## API Documentation
- Swagger UI available at `/api-docs` when the server is running.

## Azure Deployment
- See `Dockerfile` and `azure-deploy/azuredeploy.json` for deployment instructions.

---

For more details, see the OpenAPI documentation or contact the maintainer.
