import express from 'express';
import VotingSession from '../models/VotingSession';
import Vote from '../models/Vote';
import Group from '../models/Group';
import User from '../models/User';
import { verifyGoogleToken } from '../middleware.auth';
import mongoose from 'mongoose';

const router = express.Router();

// Start a voting session (owner only)
router.post('/start', verifyGoogleToken, (req, res) => {
  (async () => {
    const { groupId, movies } = req.body;
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    if (!groupId || !Array.isArray(movies) || movies.length === 0) return res.status(400).json({ error: 'groupId and movies required' });
    const group = await Group.findById(groupId);
    if (!group) return res.status(404).json({ error: 'Group not found' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    if (!group.owner || !user._id || !group.owner.equals(user._id)) return res.status(403).json({ error: 'Only the group owner can start a session' });
    if (group.votingSessionActive) return res.status(400).json({ error: 'Voting session already active' });
    const session = await VotingSession.create({ group: group._id, movies });
    group.votingSessionActive = true;
    await group.save();
    res.json({ sessionId: session._id });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// End a voting session (owner only)
router.post('/end', verifyGoogleToken, (req, res) => {
  (async () => {
    const { groupId } = req.body;
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    if (!groupId) return res.status(400).json({ error: 'groupId required' });
    const group = await Group.findById(groupId);
    if (!group) return res.status(404).json({ error: 'Group not found' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    if (!group.owner || !user._id || !group.owner.equals(user._id)) return res.status(403).json({ error: 'Only the group owner can end a session' });
    if (!group.votingSessionActive) return res.status(400).json({ error: 'No active voting session' });
    const session = await VotingSession.findOne({ group: group._id, endedAt: { $exists: false } });
    if (!session) return res.status(404).json({ error: 'Voting session not found' });
    session.endedAt = new Date();
    await session.save();
    group.votingSessionActive = false;
    await group.save();
    res.json({ sessionId: session._id });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Vote for a movie
router.post('/vote', verifyGoogleToken, (req, res) => {
  (async () => {
    const { groupId, movieId, vote } = req.body;
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    if (!groupId || !movieId || !['yes', 'no'].includes(vote)) return res.status(400).json({ error: 'groupId, movieId, and vote required' });
    const group = await Group.findById(groupId);
    if (!group) return res.status(404).json({ error: 'Group not found' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    if (!group.members.some((id: mongoose.Types.ObjectId) => id.equals(user._id))) return res.status(403).json({ error: 'User not in group' });
    // Upsert vote
    await Vote.findOneAndUpdate(
      { group: group._id, user: user._id, movieId },
      { vote },
      { upsert: true, new: true }
    );
    res.json({ success: true });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Get voting results for a session
router.get('/results/:sessionId', verifyGoogleToken, (req, res) => {
  (async () => {
    const { sessionId } = req.params;
    const session = await VotingSession.findById(sessionId);
    if (!session) return res.status(404).json({ error: 'Session not found' });
    const votes = await Vote.find({ group: session.group, movieId: { $in: session.movies } });
    // Aggregate votes per movie
    const results: Record<string, { yes: number; no: number }> = {};
    session.movies.forEach((movieId) => {
      results[movieId] = { yes: 0, no: 0 };
    });
    votes.forEach((v) => {
      if (results[v.movieId]) {
        results[v.movieId][v.vote]++;
      }
    });
    res.json({ results });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

export default router;
