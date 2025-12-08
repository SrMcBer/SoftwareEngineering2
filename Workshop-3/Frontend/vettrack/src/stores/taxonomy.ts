// src/stores/taxonomy.ts
import { defineStore } from "pinia";

export interface TaxonomyItem {
  id: string; // stable id, e.g. "dog"
  label: string; // "Dog"
  value: string; // normalized key, also "dog"
  emoji?: string;
  bgClass: string;
  textClass: string;
}

interface TaxonomyState {
  species: TaxonomyItem[];
  sex: TaxonomyItem[];
}

const STORAGE_KEY = "vettrack_taxonomy";

const defaultSpecies: TaxonomyItem[] = [
  {
    id: "dog",
    label: "Dog",
    value: "dog",
    emoji: "🐶",
    bgClass: "bg-emerald-100",
    textClass: "text-emerald-800",
  },
  {
    id: "cat",
    label: "Cat",
    value: "cat",
    emoji: "🐱",
    bgClass: "bg-sky-100",
    textClass: "text-sky-800",
  },
  {
    id: "horse",
    label: "Horse",
    value: "horse",
    emoji: "🐴",
    bgClass: "bg-amber-100",
    textClass: "text-amber-800",
  },
  {
    id: "cow",
    label: "Cow",
    value: "cow",
    emoji: "🐄",
    bgClass: "bg-violet-100",
    textClass: "text-violet-800",
  },
];

const defaultSex: TaxonomyItem[] = [
  {
    id: "m",
    label: "Male",
    value: "m",
    emoji: "♂️",
    bgClass: "bg-blue-100",
    textClass: "text-blue-800",
  },
  {
    id: "f",
    label: "Female",
    value: "f",
    emoji: "♀️",
    bgClass: "bg-pink-100",
    textClass: "text-pink-800",
  },
  {
    id: "neutered",
    label: "Neutered",
    value: "neutered",
    emoji: "⚕️",
    bgClass: "bg-slate-100",
    textClass: "text-slate-800",
  },
  {
    id: "spayed",
    label: "Spayed",
    value: "spayed",
    emoji: "⚕️",
    bgClass: "bg-slate-100",
    textClass: "text-slate-800",
  },
];

const palette = [
  { bg: "bg-emerald-100", text: "text-emerald-800" },
  { bg: "bg-sky-100", text: "text-sky-800" },
  { bg: "bg-amber-100", text: "text-amber-800" },
  { bg: "bg-violet-100", text: "text-violet-800" },
  { bg: "bg-rose-100", text: "text-rose-800" },
  { bg: "bg-slate-100", text: "text-slate-800" },
];

function normalize(value: string) {
  return value.trim().toLowerCase();
}

export const useTaxonomyStore = defineStore("taxonomy", {
  state: (): TaxonomyState => ({
    species: [],
    sex: [],
  }),

  actions: {
    init() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
          const parsed = JSON.parse(raw) as TaxonomyState;
          this.species = parsed.species ?? defaultSpecies;
          this.sex = parsed.sex ?? defaultSex;
        } else {
          this.species = defaultSpecies;
          this.sex = defaultSex;
        }
      } catch {
        this.species = defaultSpecies;
        this.sex = defaultSex;
      }
    },

    persist() {
      const data: TaxonomyState = {
        species: this.species,
        sex: this.sex,
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    },

    // ---- species ----
    findSpecies(value?: string | null): TaxonomyItem | undefined {
      if (!value) return undefined;
      const n = normalize(value);
      return (
        this.species.find(
          (s) => normalize(s.value) === n || normalize(s.label) === n
        ) ?? undefined
      );
    },

    ensureSpecies(label: string): TaxonomyItem {
      const existing = this.findSpecies(label);
      if (existing) return existing;

      const idx = this.species.length % palette.length;
      const { bg, text } = palette[idx] ?? {
        bg: "bg-slate-100",
        text: "text-slate-800",
      };
      const value = normalize(label) || `species-${this.species.length + 1}`;

      const item: TaxonomyItem = {
        id: value,
        label: label.trim(),
        value,
        emoji: "🐾",
        bgClass: bg,
        textClass: text,
      };
      this.species.push(item);
      this.persist();
      return item;
    },

    // ---- sex ----
    findSex(value?: string | null): TaxonomyItem | undefined {
      if (!value) return undefined;
      const n = normalize(value);
      return (
        this.sex.find(
          (s) => normalize(s.value) === n || normalize(s.label) === n
        ) ?? undefined
      );
    },

    ensureSex(label: string): TaxonomyItem {
      const existing = this.findSex(label);
      if (existing) return existing;

      const idx = this.sex.length % palette.length;
      const { bg, text } = palette[idx] ?? {
        bg: "bg-slate-100",
        text: "text-slate-800",
      };
      const value = normalize(label) || `sex-${this.sex.length + 1}`;

      const item: TaxonomyItem = {
        id: value,
        label: label.trim(),
        value,
        emoji: "⚧️",
        bgClass: bg,
        textClass: text,
      };
      this.sex.push(item);
      this.persist();
      return item;
    },

    // ---- helpers for badges/icons ----
    speciesBadgeClasses(species?: string | null): string {
      const item = this.findSpecies(species);
      return item
        ? `${item.bgClass} ${item.textClass}`
        : "bg-slate-100 text-slate-800";
    },

    speciesEmoji(species?: string | null): string | undefined {
      return this.findSpecies(species)?.emoji;
    },

    sexBadgeClasses(sex?: string | null): string {
      const item = this.findSex(sex);
      return item
        ? `${item.bgClass} ${item.textClass}`
        : "bg-slate-100 text-slate-800";
    },

    sexEmoji(sex?: string | null): string | undefined {
      return this.findSex(sex)?.emoji;
    },
  },

  getters: {
    // for selects
    speciesOptions: (state) => state.species,
    sexOptions: (state) => state.sex,
  },
});
