import { UserViewModel, SessionViewModel } from './viewmodels';

declare global {
  namespace Express {
    interface Request {
      user?: UserViewModel;
      session?: SessionViewModel;
    }
  }
}

export {};
