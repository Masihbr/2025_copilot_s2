import express from 'express';
import axios from 'axios';
import { verifyGoogleToken } from '../middleware.auth';

const router = express.Router();

const TMDB_API_KEY = process.env.TMDB_API_KEY;
const TMDB_BASE_URL = 'https://api.themoviedb.org/3';

// Get genres from TMDb
router.get('/genres', verifyGoogleToken, (req, res) => {
  (async () => {
    try {
      const response = await axios.get(`${TMDB_BASE_URL}/genre/movie/list`, {
        params: { api_key: TMDB_API_KEY },
      });
      res.json(response.data);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  })();
});

// Get movie details by ID
router.get('/movie/:movieId', verifyGoogleToken, (req, res) => {
  (async () => {
    try {
      const { movieId } = req.params;
      const response = await axios.get(`${TMDB_BASE_URL}/movie/${movieId}`, {
        params: { api_key: TMDB_API_KEY },
      });
      res.json(response.data);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  })();
});

// Discover movies by genre(s)
router.get('/discover', verifyGoogleToken, (req, res) => {
  (async () => {
    try {
      const { genres, page } = req.query;
      const response = await axios.get(`${TMDB_BASE_URL}/discover/movie`, {
        params: {
          api_key: TMDB_API_KEY,
          with_genres: genres,
          page: page || 1,
        },
      });
      res.json(response.data);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  })();
});

export default router;
