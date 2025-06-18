import mongoose, { Schema, Document } from 'mongoose';

export interface IUser extends Document {
  googleId: string;
  email: string;
  groups: mongoose.Types.ObjectId[];
  preferences: string[]; // genre ids
}

const UserSchema = new Schema<IUser>({
  googleId: { type: String, required: true, unique: true },
  email: { type: String, required: true },
  groups: [{ type: Schema.Types.ObjectId, ref: 'Group' }],
  preferences: [{ type: String }],
});

export default mongoose.model<IUser>('User', UserSchema);
