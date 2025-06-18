import express from 'express';
import User from '../models/User';
import { verifyGoogleToken } from '../middleware.auth';

const router = express.Router();

// Get current user info (requires Google token)
router.get('/me', verifyGoogleToken, (req, res) => {
  (async () => {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({
      googleId: user.googleId,
      email: user.email,
      groups: user.groups,
      preferences: user.preferences,
    });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Set user genre preferences
router.post('/preferences', verifyGoogleToken, (req, res) => {
  (async () => {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    const { preferences } = req.body;
    if (!Array.isArray(preferences)) return res.status(400).json({ error: 'Invalid preferences' });
    let user = await User.findOneAndUpdate(
      { googleId: req.user.googleId },
      { preferences },
      { new: true }
    );
    if (!user) {
      user = await User.create({
        googleId: req.user.googleId,
        email: req.user.email,
        preferences,
        groups: [],
      });
    }
    res.json({ preferences: user.preferences });
  })().catch((err) => res.status(500).json({ error: err.message }));
  return;
});

export default router;
