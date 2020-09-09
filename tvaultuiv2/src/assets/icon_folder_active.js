import React from 'react';

export default function iconFolderActive() {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="48"
      height="48"
      fill="none"
      viewBox="0 0 48 48"
    >
      <g filter="url(#filter0_d)">
        <rect width="40" height="40" x="4" fill="url(#paint0_linear)" rx="5" />
      </g>
      <path
        fill="url(#paint1_linear)"
        stroke="#fff"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth="2"
        d="M32.333 25.833c0 .442-.175.866-.488 1.179-.313.312-.736.488-1.178.488H17.332c-.442 0-.866-.176-1.178-.488-.313-.313-.489-.737-.489-1.179V14.167c0-.442.176-.866.489-1.179.312-.312.736-.488 1.178-.488H21.5l1.666 2.5h7.5c.442 0 .866.176 1.179.488.313.313.488.737.488 1.179v9.166z"
      />
      <defs>
        <linearGradient
          id="paint0_linear"
          x1="24"
          x2="24"
          y1="-4.407"
          y2="42.034"
          gradientUnits="userSpaceOnUse"
        >
          <stop stopColor="#FF61B2" />
          <stop offset=".509" stopColor="#E20074" />
          <stop offset="1" stopColor="#650038" />
        </linearGradient>
        <linearGradient
          id="paint1_linear"
          x1="24"
          x2="24"
          y1="10.848"
          y2="28.263"
          gradientUnits="userSpaceOnUse"
        >
          <stop stopColor="#FF61B2" />
          <stop offset=".509" stopColor="#E20074" />
          <stop offset="1" stopColor="#650038" />
        </linearGradient>
        <filter
          id="filter0_d"
          width="48"
          height="48"
          x="0"
          y="0"
          colorInterpolationFilters="sRGB"
          filterUnits="userSpaceOnUse"
        >
          <feFlood floodOpacity="0" result="BackgroundImageFix" />
          <feColorMatrix
            in="SourceAlpha"
            values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
          />
          <feOffset dy="4" />
          <feGaussianBlur stdDeviation="2" />
          <feColorMatrix values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0" />
          <feBlend in2="BackgroundImageFix" result="effect1_dropShadow" />
          <feBlend in="SourceGraphic" in2="effect1_dropShadow" result="shape" />
        </filter>
      </defs>
    </svg>
  );
}
