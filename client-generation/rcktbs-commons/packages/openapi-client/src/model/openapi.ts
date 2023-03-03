// Generated using typescript-generator version 3.1.1185 on 2023-02-27 14:28:43.

export interface Activity {
    type: "comment" | "dossier";
    id: string;
    dated: Date;
    user: string;
}

export interface CommentActivity extends Activity {
    type: "comment";
    comment: string;
    attachments: string[];
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
    created: Date;
    lastModified: Date;
}

export type TileType = "search-config" | "pinboard" | "briefing" | "showroom";

export type UserPreference = "favored" | "disfavored";

export type ActivityUnion = CommentActivity | RecommendationActivity;
