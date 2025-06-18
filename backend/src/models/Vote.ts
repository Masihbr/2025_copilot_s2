import mongoose, { Schema, Document } from 'mongoose';

export interface IVote extends Document {
  group: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  movieId: string; // TMDb movie ID
  vote: 'yes' | 'no';
}

const VoteSchema = new Schema<IVote>({
  group: { type: Schema.Types.ObjectId, ref: 'Group', required: true },
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  movieId: { type: String, required: true },
  vote: { type: String, enum: ['yes', 'no'], required: true },
});

export default mongoose.model<IVote>('Vote', VoteSchema);
