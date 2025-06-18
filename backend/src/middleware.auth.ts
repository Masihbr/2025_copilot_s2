import { Request, Response, NextFunction } from 'express';
import { OAuth2Client } from 'google-auth-library';

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export async function verifyGoogleToken(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or invalid Authorization header' });
  }
  const token = authHeader.split(' ')[1];
  try {
    const ticket = await client.verifyIdToken({
      idToken: token,
      audience: process.env.GOOGLE_CLIENT_ID,
    });
    const payload = ticket.getPayload();
    if (!payload || !payload.sub) {
      return res.status(401).json({ error: 'Invalid Google token' });
    }
    // Attach user info to request
    req.user = {
      googleId: payload.sub,
      email: payload.email,
    };
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Google token verification failed' });
  }
}

declare global {
  namespace Express {
    interface Request {
      user?: {
        googleId: string;
        email?: string;
      };
    }
  }
}
