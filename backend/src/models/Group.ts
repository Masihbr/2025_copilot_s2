import mongoose, { Schema, Document } from 'mongoose';

export interface IGroup extends Document {
  name: string;
  owner: mongoose.Types.ObjectId;
  members: mongoose.Types.ObjectId[];
  inviteCode: string;
  votingSessionActive: boolean;
}

const GroupSchema = new Schema<IGroup>({
  name: { type: String, required: true },
  owner: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  members: [{ type: Schema.Types.ObjectId, ref: 'User' }],
  inviteCode: { type: String, required: true, unique: true },
  votingSessionActive: { type: Boolean, default: false },
});

export default mongoose.model<IGroup>('Group', GroupSchema);
