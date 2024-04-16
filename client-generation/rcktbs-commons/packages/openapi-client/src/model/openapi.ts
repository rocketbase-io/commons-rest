// Generated using typescript-generator version 3.2.1263 on 2024-04-16 18:09:05.

export interface Activity {
  type: "comment" | "dossier";
  id: string;
  dated: string;
  user: string;
}

export interface CommentActivity extends Activity {
  type: "comment";
  comment: string;
  attachments: string[];
}

export interface PermissionCmd {
  objectId: string;
  identityIds: string[];
  permissionId: number;
}

export interface RecommendationActivity extends Activity {
  type: "dossier";
  objectId: string;
  objectTitle: string;
  message: string;
}

export interface Tile {
  id: string;
  type: TileType;
  name: string;
  description: string;
  commentCount: number;
  itemCount: number;
  userPreference: UserPreference;
  created: string;
  lastModified: string;
}

export type ActivityUnion = CommentActivity | RecommendationActivity;

export enum TileType {
  SEARCH_CONFIG = "search-config",
  PINBOARD = "pinboard",
  BRIEFING = "briefing",
  SHOWROOM = "showroom",
}

export enum UserPreference {
  FAVORED = "favored",
  DISFAVORED = "disfavored",
}
