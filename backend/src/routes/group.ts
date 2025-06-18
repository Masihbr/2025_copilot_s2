import express from 'express';
import Group from '../models/Group';
import User from '../models/User';
import { verifyGoogleToken } from '../middleware.auth';
import crypto from 'crypto';
import mongoose from 'mongoose';

const router = express.Router();

// Create a group
router.post('/create', verifyGoogleToken, (req, res) => {
  (async () => {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    const { name } = req.body;
    if (!name) return res.status(400).json({ error: 'Group name required' });
    const inviteCode = crypto.randomBytes(4).toString('hex');
    const ownerUser = await User.findOne({ googleId: req.user.googleId });
    if (!ownerUser) return res.status(404).json({ error: 'User not found' });
    const group = await Group.create({
      name,
      owner: ownerUser._id,
      members: [ownerUser._id],
      inviteCode,
      votingSessionActive: false,
    });
    // Add group to user
    await User.findByIdAndUpdate(ownerUser._id, { $addToSet: { groups: group._id } }, { new: true });
    res.json({ groupId: group._id, inviteCode });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Join a group by invite code
router.post('/join', verifyGoogleToken, (req, res) => {
  (async () => {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    const { inviteCode } = req.body;
    if (!inviteCode) return res.status(400).json({ error: 'Invite code required' });
    const group = await Group.findOne({ inviteCode });
    if (!group) return res.status(404).json({ error: 'Group not found' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    // Add user to group
    if (!group.members.some((id) => id.equals(user._id))) {
      group.members.push(user._id);
      await group.save();
    }
    // Add group to user
    await User.findByIdAndUpdate(user._id, { $addToSet: { groups: group._id } }, { new: true });
    res.json({ groupId: group._id });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Delete a group (owner only)
router.delete('/:groupId', verifyGoogleToken, (req, res) => {
  (async () => {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });
    const { groupId } = req.params;
    const group = await Group.findById(groupId);
    if (!group) return res.status(404).json({ error: 'Group not found' });
    const user = await User.findOne({ googleId: req.user.googleId });
    if (!user) return res.status(404).json({ error: 'User not found' });
    if (!group.owner.equals(user._id)) {
      return res.status(403).json({ error: 'Only the group owner can delete the group' });
    }
    await group.deleteOne();
    // Remove group from all users
    await User.updateMany({ groups: groupId }, { $pull: { groups: groupId } });
    res.json({ success: true });
  })().catch((err) => res.status(500).json({ error: err.message }));
});

// Get group info
router.get('/:groupId', verifyGoogleToken, (req, res) => {
  (async () => {
    const { groupId } = req.params;
    const group = await Group.findById(groupId).populate('members', 'googleId email preferences');
    if (!group) return res.status(404).json({ error: 'Group not found' });
    res.json(group);
  })().catch((err) => res.status(500).json({ error: err.message }));
});

export default router;
