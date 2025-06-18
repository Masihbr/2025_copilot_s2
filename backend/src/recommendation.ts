import User from './models/User';
import Group from './models/Group';
import VotingSession from './models/VotingSession';
import Vote from './models/Vote';

/**
 * Recommend movies for a group based on genre preferences.
 * Prioritizes genres common to all members, then individual preferences.
 * Returns a sorted list of TMDb genre IDs.
 */
export async function recommendGenresForGroup(groupId: string): Promise<string[]> {
  const group = await Group.findById(groupId).populate('members');
  if (!group) return [];
  const members = group.members as any[];
  if (!members.length) return [];
  // Collect all preferences
  const allPrefs = members.map((m) => m.preferences || []);
  // Find intersection (common genres)
  const common = allPrefs.reduce((a, b) => a.filter((v: string) => b.includes(v)));
  // If no common, flatten and count frequency
  if (common.length > 0) return common;
  const freq: Record<string, number> = {};
  allPrefs.flat().forEach((g: string) => { freq[g] = (freq[g] || 0) + 1; });
  return Object.entries(freq)
    .sort((a, b) => b[1] - a[1])
    .map(([g]) => g);
}
