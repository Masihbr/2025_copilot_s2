import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';
import dotenv from 'dotenv';
import swaggerUi from 'swagger-ui-express';
import YAML from 'yamljs';
import userRouter from './routes/user';
import groupRouter from './routes/group';
import votingRouter from './routes/voting';
import movieRouter from './routes/movie';
import { recommendGenresForGroup } from './recommendation';

// Load environment variables
dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// MongoDB connection
mongoose.connect(process.env.MONGODB_URI || '')
  .then(() => console.log('MongoDB connected'))
  .catch((err) => console.error('MongoDB connection error:', err));

// Swagger/OpenAPI setup
const swaggerDocument = YAML.load('./openapi.yaml');
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use('/user', userRouter);
app.use('/group', groupRouter);
app.use('/voting', votingRouter);
app.use('/movie', movieRouter);

// Recommend movies for a group (returns sorted genre IDs)
app.get('/group/:groupId/recommend', (req, res) => {
  (async () => {
    const { groupId } = req.params;
    const genres = await recommendGenresForGroup(groupId);
    res.json({ genres });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

app.get('/health', (_req, res) => {
  res.json({ status: 'ok' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
