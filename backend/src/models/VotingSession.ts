import mongoose, { Schema, Document } from 'mongoose';

export interface IVotingSession extends Document {
  group: mongoose.Types.ObjectId;
  startedAt: Date;
  endedAt?: Date;
  movies: string[]; // TMDb movie IDs
}

const VotingSessionSchema = new Schema<IVotingSession>({
  group: { type: Schema.Types.ObjectId, ref: 'Group', required: true },
  startedAt: { type: Date, default: Date.now },
  endedAt: { type: Date },
  movies: [{ type: String }],
});

export default mongoose.model<IVotingSession>('VotingSession', VotingSessionSchema);
